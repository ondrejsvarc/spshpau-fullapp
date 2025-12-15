import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { createProjectExpenseApi } from '../services/api';
import { useUser } from '../contexts/UserContext';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Grid from '@mui/material/Grid';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { isValid, formatISO } from 'date-fns';
import {AdapterDateFns} from "@mui/x-date-pickers/AdapterDateFns";

function CreateExpensePage() {
    const { projectId } = useParams();
    const navigate = useNavigate();
    const { keycloakAuthenticated } = useUser();

    const [formData, setFormData] = useState({
        amount: '',
        date: new Date(),
        comment: '',
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData((prev) => ({
            ...prev,
            [name]: name === 'amount' ? (value === '' ? '' : parseFloat(value)) : value,
        }));
    };

    const handleDateChange = (newDate) => {
        setFormData((prev) => ({ ...prev, date: newDate }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (!formData.amount || formData.amount <= 0) {
            setError('Amount must be a positive number.');
            return;
        }
        if (!formData.date || !isValid(formData.date)) {
            setError('Please select a valid date.');
            return;
        }
        if (!formData.comment.trim()) {
            setError('Comment is required.');
            return;
        }
        if (!keycloakAuthenticated) {
            setError('You must be logged in.');
            return;
        }

        setLoading(true);
        setError(null);
        setSuccess(null);

        try {
            const expensePayload = {
                amount: parseFloat(formData.amount),
                date: formatISO(formData.date),
                comment: formData.comment.trim(),
            };
            console.log("Submitting expense:", expensePayload);
            await createProjectExpenseApi(projectId, expensePayload);
            setSuccess('Expense added successfully! Redirecting...');
            setTimeout(() => {
                navigate(`/projects/${projectId}/budget`);
            }, 1500);
        } catch (err) {
            console.error("Failed to create expense:", err);
            setError(err.message || 'Failed to add expense.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <LocalizationProvider dateAdapter={AdapterDateFns}>
            <Container maxWidth="sm">
                <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(`/projects/${projectId}/budget`)} sx={{ mt: 2, mb: 1 }}>
                    Back to Budget
                </Button>
                <Paper elevation={3} sx={{ p: { xs: 2, sm: 4 }, mt: 2, mb: 4 }}>
                    <Typography variant="h4" component="h1" gutterBottom align="center">
                        Add New Expense
                    </Typography>
                    <Typography variant="caption" display="block" align="center" color="textSecondary" sx={{mb:2}}>
                        For Project ID: {projectId}
                    </Typography>

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                    {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                    {!success && (
                        <Box component="form" onSubmit={handleSubmit} noValidate>
                            <Grid container spacing={2}>
                                <Grid item xs={12} sm={6}>
                                    <TextField
                                        margin="dense"
                                        required
                                        fullWidth
                                        id="amount"
                                        label="Amount"
                                        name="amount"
                                        type="number"
                                        value={formData.amount}
                                        onChange={handleChange}
                                        autoFocus
                                        error={!!error && (!formData.amount || formData.amount <= 0)}
                                        helperText={!!error && (!formData.amount || formData.amount <= 0) ? "Must be positive" : ""}
                                        disabled={loading}
                                        InputProps={{ inputProps: { step: "0.01", min: "0.01" } }}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <DatePicker
                                        label="Date of Expense"
                                        value={formData.date}
                                        onChange={handleDateChange}
                                        slotProps={{ textField: { fullWidth: true, margin:"dense", required: true, error: !!error && (!formData.date || !isValid(formData.date)) } }}
                                        disabled={loading}
                                    />
                                    {!!error && (!formData.date || !isValid(formData.date)) && <Typography color="error" variant="caption">Valid date required</Typography>}
                                </Grid>
                                <Grid item xs={12}>
                                    <TextField
                                        margin="dense"
                                        required
                                        fullWidth
                                        id="comment"
                                        label="Comment / Description"
                                        name="comment"
                                        multiline
                                        rows={3}
                                        value={formData.comment}
                                        onChange={handleChange}
                                        error={!!error && !formData.comment.trim()}
                                        helperText={!!error && !formData.comment.trim() ? "Comment is required" : ""}
                                        disabled={loading}
                                    />
                                </Grid>
                            </Grid>
                            <Button
                                type="submit"
                                fullWidth
                                variant="contained"
                                sx={{ mt: 3, mb: 2, py: 1.5 }}
                                disabled={loading || !formData.amount || formData.amount <= 0 || !formData.date || !isValid(formData.date) || !formData.comment.trim()}
                            >
                                {loading ? <CircularProgress size={24} color="inherit" /> : 'Add Expense'}
                            </Button>
                        </Box>
                    )}
                </Paper>
            </Container>
        </LocalizationProvider>
    );
}

export default CreateExpensePage;