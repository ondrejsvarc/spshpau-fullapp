import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import {
    getProjectById,
    getProjectBudget,
    updateProjectBudgetApi,
    getProjectExpenses,
    deleteProjectExpenseApi
} from '../services/api';
import ExpenseCard from '../components/ExpenseCard';
import { HorizontalScrollSection } from './ProjectDetailPage';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';
import Pagination from '@mui/material/Pagination';


function BudgetPage() {
    const { projectId } = useParams();
    const navigate = useNavigate();
    const { appUser, keycloakAuthenticated } = useUser();

    const [projectTitle, setProjectTitle] = useState('');
    const [budgetDetails, setBudgetDetails] = useState(null);
    const [expenses, setExpenses] = useState([]);
    const [isOwner, setIsOwner] = useState(false);

    const [loadingBudget, setLoadingBudget] = useState(true);
    const [loadingExpenses, setLoadingExpenses] = useState(true);
    const [error, setError] = useState(null);

    const [isEditingBudget, setIsEditingBudget] = useState(false);
    const [editBudgetData, setEditBudgetData] = useState({ totalAmount: '', currency: '' });
    const [editLoading, setEditLoading] = useState(false);

    const [expensePage, setExpensePage] = useState(1);
    const [expenseTotalPages, setExpenseTotalPages] = useState(0);

    const [confirmDeleteDialogOpen, setConfirmDeleteDialogOpen] = useState(false);
    const [expenseToDelete, setExpenseToDelete] = useState(null);


    const fetchBudgetAndProjectData = useCallback(async () => {
        if (!keycloakAuthenticated || !projectId) return;
        setLoadingBudget(true);
        setError(null);
        try {
            const projectData = await getProjectById(projectId);
            setProjectTitle(projectData.title || 'Project');
            if (appUser && projectData.owner && appUser.id === projectData.owner.id) {
                setIsOwner(true);
            } else {
                setIsOwner(false);
            }

            // Fetch budget details
            const budgetData = await getProjectBudget(projectId);
            setBudgetDetails(budgetData);
            setEditBudgetData({
                totalAmount: budgetData.totalAmount || '',
                currency: budgetData.currency || 'USD'
            });
        } catch (err) {
            console.error("Failed to fetch budget/project details:", err);
            if (String(err.message).includes("404")) {
                setBudgetDetails(null);
            } else {
                setError(err.message || "Could not load budget details.");
            }
        } finally {
            setLoadingBudget(false);
        }
    }, [projectId, keycloakAuthenticated, appUser]);

    const fetchExpensesData = useCallback(async (currentPage) => {
        if (!keycloakAuthenticated || !projectId || !budgetDetails) return;
        setLoadingExpenses(true);
        try {
            const expensesData = await getProjectExpenses(projectId, currentPage - 1, 5);
            setExpenses(expensesData.content || []);
            setExpenseTotalPages(expensesData.totalPages || 0);
        } catch (err) {
            console.error("Failed to fetch expenses:", err);
            setError(err.message || "Could not load expenses.");
        } finally {
            setLoadingExpenses(false);
        }
    }, [projectId, keycloakAuthenticated, budgetDetails]);


    useEffect(() => {
        fetchBudgetAndProjectData();
    }, [fetchBudgetAndProjectData]);

    useEffect(() => {
        if (budgetDetails && budgetDetails.projectId) {
            fetchExpensesData(expensePage);
        } else {
            setExpenses([]);
            setLoadingExpenses(false);
            setExpenseTotalPages(0);
        }
    }, [budgetDetails, expensePage, fetchExpensesData]);


    const handleBudgetEditToggle = () => {
        if (!isEditingBudget && budgetDetails) {
            setEditBudgetData({
                totalAmount: budgetDetails.totalAmount || '',
                currency: budgetDetails.currency || 'USD',
            });
        }
        setIsEditingBudget(!isEditingBudget);
    };

    const handleBudgetInputChange = (event) => {
        const { name, value } = event.target;
        setEditBudgetData(prev => ({
            ...prev,
            [name]: name === 'totalAmount' ? (value === '' ? '' : parseFloat(value)) : value.toUpperCase()
        }));
    };

    const handleSaveBudget = async () => {
        if (!editBudgetData.totalAmount || editBudgetData.totalAmount <= 0) {
            alert('Total Amount must be positive.'); return;
        }
        if (!editBudgetData.currency || editBudgetData.currency.trim().length !== 3) {
            alert('Currency must be 3 letters.'); return;
        }
        setEditLoading(true);
        try {
            const updatedBudget = await updateProjectBudgetApi(projectId, {
                totalAmount: parseFloat(editBudgetData.totalAmount),
                currency: editBudgetData.currency.trim().toUpperCase(),
            });
            setBudgetDetails(updatedBudget);
            setIsEditingBudget(false);
        } catch (err) {
            console.error("Failed to update budget:", err);
            alert(`Error updating budget: ${err.message}`);
        } finally {
            setEditLoading(false);
        }
    };

    const openDeleteExpenseConfirm = (expenseId) => {
        setExpenseToDelete(expenseId);
        setConfirmDeleteDialogOpen(true);
    };

    const handleDeleteExpense = async () => {
        if (!expenseToDelete) return;
        try {
            await deleteProjectExpenseApi(projectId, expenseToDelete);
            setConfirmDeleteDialogOpen(false);
            setExpenseToDelete(null);
            fetchExpensesData(expensePage);
        } catch (err) {
            console.error("Failed to delete expense:", err);
            alert(`Error deleting expense: ${err.message}`);
            setConfirmDeleteDialogOpen(false);
        }
    };

    const handleExpensePageChange = (event, value) => {
        setExpensePage(value);
    };


    if (loadingBudget && !budgetDetails && !isOwner) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 5 }}><CircularProgress /></Box>;
    }
    if (error) {
        return <Container sx={{mt:3}}><Alert severity="error">{error}</Alert></Container>;
    }


    return (
        <Container maxWidth="lg" sx={{ py: 2 }}>
            <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(`/projects/${projectId}`)} sx={{ mb: 2 }}>
                Back to Project Details
            </Button>
            <Typography variant="h4" component="h1" gutterBottom>
                Project Budget: {projectTitle}
            </Typography>

            {loadingBudget ? <CircularProgress sx={{display:'block', mx:'auto', my:3}}/> :
                !budgetDetails && isOwner ? (
                    <Paper sx={{ p: 3, textAlign: 'center', mt:2 }}>
                        <Typography gutterBottom>No budget has been set for this project yet.</Typography>
                        <Button variant="contained" onClick={() => navigate(`/projects/${projectId}/budget/create`)}>
                            Create Budget
                        </Button>
                    </Paper>
                ) : !budgetDetails && !isOwner ? (
                    <Paper sx={{ p: 3, textAlign: 'center', mt:2 }}>
                        <Typography color="textSecondary">No budget information available for this project.</Typography>
                    </Paper>
                ) : budgetDetails && (
                    <Paper elevation={2} sx={{ p: {xs:2, sm:3}, mb: 4 }}>
                        <Typography variant="h5" component="h2" gutterBottom>Budget Overview</Typography>
                        {isEditingBudget ? (
                            <Box component="form" onSubmit={(e) => {e.preventDefault(); handleSaveBudget();}}>
                                <Grid container spacing={2} alignItems="center">
                                    <Grid item xs={12} sm={5}>
                                        <TextField
                                            label="Total Amount"
                                            name="totalAmount"
                                            type="number"
                                            value={editBudgetData.totalAmount}
                                            onChange={handleBudgetInputChange}
                                            fullWidth
                                            required
                                            InputProps={{ inputProps: { step: "0.01", min:"0.01" } }}
                                            disabled={editLoading}
                                        />
                                    </Grid>
                                    <Grid item xs={12} sm={3}>
                                        <TextField
                                            label="Currency"
                                            name="currency"
                                            value={editBudgetData.currency}
                                            onChange={handleBudgetInputChange}
                                            fullWidth
                                            required
                                            inputProps={{ maxLength: 3, style:{textTransform:'uppercase'} }}
                                            disabled={editLoading}
                                        />
                                    </Grid>
                                    <Grid item xs={12} sm={4}>
                                        <Button type="submit" variant="contained" startIcon={editLoading ? <CircularProgress size={20}/> : <SaveIcon />} disabled={editLoading} sx={{mr:1}}>Save</Button>
                                        <Button variant="outlined" onClick={handleBudgetEditToggle} startIcon={<CancelIcon />} disabled={editLoading}>Cancel</Button>
                                    </Grid>
                                </Grid>
                            </Box>
                        ) : (
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap:'wrap' }}>
                                <Box>
                                    <Typography variant="h6">{budgetDetails.totalAmount?.toFixed(2)} {budgetDetails.currency}</Typography>
                                    <Typography color="textSecondary">Total Budgeted</Typography>
                                    <Typography variant="body1" sx={{color: budgetDetails.remainingAmount < 0 ? 'error.main' : 'success.main', mt:0.5}}>
                                        Remaining: {budgetDetails.remainingAmount?.toFixed(2)} {budgetDetails.currency}
                                    </Typography>
                                    <Typography variant="caption" display="block">Spent: {budgetDetails.spentAmount?.toFixed(2)} {budgetDetails.currency}</Typography>
                                </Box>
                                {isOwner && (
                                    <IconButton onClick={handleBudgetEditToggle} aria-label="edit budget">
                                        <EditIcon />
                                    </IconButton>
                                )}
                            </Box>
                        )}
                    </Paper>
                )}

            { budgetDetails && (
                <>
                    <HorizontalScrollSection
                        title="Expenses"
                        loading={loadingExpenses}
                        noDataMessage="No expenses logged yet."
                        actionButton={
                            <Button variant="outlined" size="small" startIcon={<AddCircleOutlineIcon />} onClick={() => navigate(`/projects/${projectId}/budget/expenses/new`)}>
                                Add Expense
                            </Button>
                        }
                    >
                        {expenses.map(exp => <ExpenseCard key={exp.id} expense={exp} currency={budgetDetails?.currency} onDelete={() => openDeleteExpenseConfirm(exp.id)} />)}
                    </HorizontalScrollSection>
                    {expenseTotalPages > 1 && !loadingExpenses && (
                        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                            <Pagination count={expenseTotalPages} page={expensePage} onChange={handleExpensePageChange} color="primary" size="small" />
                        </Box>
                    )}
                </>
            )}


            {/* Delete Expense Confirmation Dialog */}
            <Dialog open={confirmDeleteDialogOpen} onClose={() => setConfirmDeleteDialogOpen(false)}>
                <DialogTitle>Delete Expense?</DialogTitle>
                <DialogContent><DialogContentText>Are you sure you want to delete this expense? This action cannot be undone.</DialogContentText></DialogContent>
                <DialogActions>
                    <Button onClick={() => setConfirmDeleteDialogOpen(false)}>Cancel</Button>
                    <Button onClick={handleDeleteExpense} color="error" autoFocus>Delete</Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
}

export default BudgetPage;