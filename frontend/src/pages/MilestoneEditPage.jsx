import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import { getProjectMilestoneByIdApi, updateProjectMilestoneApi, deleteProjectMilestoneApi } from '../services/api';

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
import DeleteIcon from '@mui/icons-material/Delete';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { isValid, formatISO, parseISO } from 'date-fns';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';
import {AdapterDateFns} from "@mui/x-date-pickers/AdapterDateFns";


function MilestoneEditPage() {
    const { projectId, milestoneId } = useParams();
    const navigate = useNavigate();
    const { keycloakAuthenticated } = useUser();

    const [formData, setFormData] = useState({
        title: '',
        description: '',
        dueDate: null,
    });
    const [initialLoading, setInitialLoading] = useState(true);
    const [loading, setLoading] = useState(false); // For save/delete actions
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [confirmDeleteDialogOpen, setConfirmDeleteDialogOpen] = useState(false);


    const fetchMilestone = useCallback(async () => {
        if (!keycloakAuthenticated) return;
        setInitialLoading(true);
        setError(null);
        try {
            const milestoneData = await getProjectMilestoneByIdApi(projectId, milestoneId);
            setFormData({
                title: milestoneData.title || '',
                description: milestoneData.description || '',
                dueDate: milestoneData.dueDate ? parseISO(milestoneData.dueDate) : null,
            });
        } catch (err) {
            console.error("Failed to fetch milestone:", err);
            setError(err.message || 'Could not load milestone data.');
        } finally {
            setInitialLoading(false);
        }
    }, [projectId, milestoneId, keycloakAuthenticated]);

    useEffect(() => {
        fetchMilestone();
    }, [fetchMilestone]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleDateChange = (newDate) => {
        setFormData((prev) => ({ ...prev, dueDate: newDate }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (!formData.title.trim()) {
            setError('Milestone Title is required.');
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
            const milestonePayload = {
                title: formData.title.trim(),
                description: formData.description.trim() || null,
                dueDate: formData.dueDate && isValid(formData.dueDate) ? formatISO(formData.dueDate) : null,
            };
            console.log('Updating milestone with payload:', milestonePayload);
            await updateProjectMilestoneApi(projectId, milestoneId, milestonePayload);
            setSuccess(`Milestone "${milestonePayload.title}" updated! Redirecting...`);
            setTimeout(() => {
                navigate(`/projects/${projectId}`);
            }, 1500);
        } catch (err) {
            console.error("Failed to update milestone:", err);
            setError(err.message || 'Failed to update milestone.');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async () => {
        setConfirmDeleteDialogOpen(false);
        setLoading(true);
        setError(null);
        try {
            await deleteProjectMilestoneApi(projectId, milestoneId);
            setSuccess(`Milestone deleted successfully! Redirecting...`);
            setTimeout(() => {
                navigate(`/projects/${projectId}`);
            }, 1500);
        } catch (err) {
            console.error("Failed to delete milestone:", err);
            setError(err.message || 'Failed to delete milestone.');
            setLoading(false);
        }
    };


    if (initialLoading) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}><CircularProgress /></Box>;
    }
    if (error && !formData.title) { // If error during initial load
        return <Container sx={{mt:2}}><Alert severity="error">{error}</Alert></Container>
    }


    return (
        <LocalizationProvider dateAdapter={AdapterDateFns}>
            <Container maxWidth="sm">
                <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(`/projects/${projectId}`)} sx={{ mt: 2, mb: 1 }}>
                    Back to Project
                </Button>
                <Paper elevation={3} sx={{ p: { xs: 2, sm: 4 }, mt: 2, mb: 4 }}>
                    <Typography variant="h4" component="h1" gutterBottom align="center">
                        Edit Milestone
                    </Typography>
                    <Typography variant="caption" display="block" align="center" color="textSecondary" sx={{mb:2}}>
                        Project ID: {projectId} | Milestone ID: {milestoneId}
                    </Typography>

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                    {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                    {!success && (
                        <Box component="form" onSubmit={handleSubmit} noValidate>
                            <Grid container spacing={2}>
                                <Grid item xs={12}>
                                    <TextField
                                        margin="dense"
                                        required
                                        fullWidth
                                        id="title"
                                        label="Milestone Title"
                                        name="title"
                                        value={formData.title}
                                        onChange={handleChange}
                                        autoFocus
                                        error={!!error && !formData.title?.trim()}
                                        helperText={!!error && !formData.title?.trim() ? "Title is required" : ""}
                                        disabled={loading}
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    <TextField
                                        margin="dense"
                                        fullWidth
                                        id="description"
                                        label="Description (Optional)"
                                        name="description"
                                        multiline
                                        rows={4}
                                        value={formData.description}
                                        onChange={handleChange}
                                        disabled={loading}
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    <DateTimePicker
                                        label="Due Date (Optional)"
                                        value={formData.dueDate}
                                        onChange={handleDateChange}
                                        slotProps={{ textField: { fullWidth: true, margin:"dense", variant:"outlined" } }}
                                        disabled={loading}
                                    />
                                </Grid>
                            </Grid>
                            <Box sx={{display: 'flex', justifyContent: 'space-between', mt: 3, mb: 2}}>
                                <Button
                                    variant="outlined"
                                    color="error"
                                    onClick={() => setConfirmDeleteDialogOpen(true)}
                                    disabled={loading}
                                    startIcon={<DeleteIcon/>}
                                >
                                    Delete
                                </Button>
                                <Button
                                    type="submit"
                                    variant="contained"
                                    disabled={loading || !formData.title?.trim()}
                                >
                                    {loading ? <CircularProgress size={24} color="inherit" /> : 'Save Changes'}
                                </Button>
                            </Box>
                        </Box>
                    )}
                </Paper>

                {/* Delete Confirmation Dialog */}
                <Dialog open={confirmDeleteDialogOpen} onClose={() => setConfirmDeleteDialogOpen(false)}>
                    <DialogTitle>Delete Milestone?</DialogTitle>
                    <DialogContent><DialogContentText>Are you sure you want to delete this milestone: "{formData.title || 'this milestone'}"?</DialogContentText></DialogContent>
                    <DialogActions>
                        <Button onClick={() => setConfirmDeleteDialogOpen(false)}>Cancel</Button>
                        <Button onClick={handleDelete} color="error">Delete</Button>
                    </DialogActions>
                </Dialog>
            </Container>
        </LocalizationProvider>
    );
}

export default MilestoneEditPage;