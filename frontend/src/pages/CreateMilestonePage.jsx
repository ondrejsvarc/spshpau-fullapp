import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import { createProjectMilestoneApi } from '../services/api';

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
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { isValid, formatISO } from 'date-fns';
import {AdapterDateFns} from "@mui/x-date-pickers/AdapterDateFns";


function CreateMilestonePage() {
    const { projectId } = useParams();
    const navigate = useNavigate();
    const { keycloakAuthenticated } = useUser();

    const [formData, setFormData] = useState({
        title: '',
        description: '',
        dueDate: null,
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

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

            console.log('Creating milestone with payload:', milestonePayload);
            const createdMilestone = await createProjectMilestoneApi(projectId, milestonePayload);
            setSuccess(`Milestone "${createdMilestone.title}" created! Redirecting...`);
            setTimeout(() => {
                navigate(`/projects/${projectId}`);
            }, 1500);

        } catch (err) {
            console.error("Failed to create milestone:", err);
            setError(err.message || 'Failed to create milestone.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <LocalizationProvider dateAdapter={AdapterDateFns}>
            <Container maxWidth="sm">
                <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(`/projects/${projectId}`)} sx={{ mt: 2, mb: 1 }}>
                    Back to Project
                </Button>
                <Paper elevation={3} sx={{ p: { xs: 2, sm: 4 }, mt: 2, mb: 4 }}>
                    <Typography variant="h4" component="h1" gutterBottom align="center">
                        Create New Milestone
                    </Typography>
                    <Typography variant="caption" display="block" align="center" color="textSecondary" sx={{mb:2}}>
                        For Project ID: {projectId}
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
                                        error={!!error && !formData.title.trim()}
                                        helperText={!!error && !formData.title.trim() ? "Title is required" : ""}
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
                            <Button
                                type="submit"
                                fullWidth
                                variant="contained"
                                sx={{ mt: 3, mb: 2, py: 1.5 }}
                                disabled={loading || !formData.title.trim()}
                            >
                                {loading ? <CircularProgress size={24} color="inherit" /> : 'Create Milestone'}
                            </Button>
                        </Box>
                    )}
                </Paper>
            </Container>
        </LocalizationProvider>
    );
}

export default CreateMilestonePage;