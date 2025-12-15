import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import { createProjectTaskApi, getProjectCollaborators } from '../services/api';

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
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { isValid, formatISO } from 'date-fns';
import {AdapterDateFns} from "@mui/x-date-pickers/AdapterDateFns";

const taskStatusOptions = ['TODO', 'IN_PROGRESS', 'DONE', 'WAITING', 'REVIEW', 'BLOCKED'];

function CreateTaskPage() {
    const { projectId } = useParams();
    const navigate = useNavigate();
    const { keycloakAuthenticated, appUser } = useUser();

    const [formData, setFormData] = useState({
        title: '',
        description: '',
        dueDate: null,
        status: 'TODO',
        assignedUserId: '',
    });
    const [collaborators, setCollaborators] = useState([]);
    const [loading, setLoading] = useState(false);
    const [formError, setFormError] = useState(null);
    const [fieldErrors, setFieldErrors] = useState({});
    const [success, setSuccess] = useState(null);

    useEffect(() => {
        const fetchCollaborators = async () => {
            if (keycloakAuthenticated && projectId) {
                try {
                    const data = await getProjectCollaborators(projectId, 0, 100);

                    setCollaborators(data.content || []);


                } catch (err) {
                    console.error("Failed to fetch project collaborators:", err);
                    setFormError("Could not load assignable users for the task.");
                }
            }
        };
        fetchCollaborators();
    }, [projectId, keycloakAuthenticated, appUser]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        if (fieldErrors[name]) {
            setFieldErrors(prev => ({...prev, [name]: null}));
        }
    };

    const handleDateChange = (newDate) => {
        setFormData((prev) => ({ ...prev, dueDate: newDate }));
        if (fieldErrors.dueDate) {
            setFieldErrors(prev => ({...prev, dueDate: null}));
        }
    };

    const validateForm = () => {
        const newErrors = {};
        if (!formData.title.trim()) newErrors.title = 'Title is required.';
        else if (formData.title.trim().length < 3) newErrors.title = 'Title must be at least 3 characters.';
        else if (formData.title.trim().length > 255) newErrors.title = 'Title cannot exceed 255 characters.';

        if (formData.description.trim().length > 5000) newErrors.description = 'Description cannot exceed 5000 characters.';
        if (!formData.status) newErrors.status = 'Status is required.';
        if (formData.dueDate && !isValid(formData.dueDate)) newErrors.dueDate = 'Invalid due date.';

        setFieldErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };


    const handleSubmit = async (event) => {
        event.preventDefault();
        if (!validateForm()) return;
        if (!keycloakAuthenticated) {
            setFormError('You must be logged in to create a task.');
            return;
        }

        setLoading(true);
        setFormError(null);
        setSuccess(null);

        try {
            const taskPayload = {
                title: formData.title.trim(),
                description: formData.description.trim() || null,
                status: formData.status,
                dueDate: formData.dueDate && isValid(formData.dueDate) ? formatISO(formData.dueDate) : null,
                assignedUserId: formData.assignedUserId || null,
            };

            console.log('Creating task with payload:', taskPayload);
            const createdTask = await createProjectTaskApi(projectId, taskPayload);
            setSuccess(`Task "${createdTask.title}" created! Redirecting...`);
            setTimeout(() => {
                navigate(`/projects/${projectId}`);
            }, 1500);

        } catch (err) {
            console.error("Failed to create task:", err);
            let errorMessage = 'Failed to create task. Please try again.';
            if (err.message && err.message.includes("API Error")) {
                const apiErrorMsgPart = err.message.substring(err.message.indexOf(':') + 1).trim();
                errorMessage = apiErrorMsgPart || errorMessage;
            } else if (err.message) {
                errorMessage = err.message;
            }
            setFormError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    return (
        <LocalizationProvider dateAdapter={AdapterDateFns}>
            <Container maxWidth="md">
                <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(`/projects/${projectId}`)} sx={{ mt: 2, mb: 1 }}>
                    Back to Project
                </Button>
                <Paper elevation={3} sx={{ p: { xs: 2, sm: 4 }, mt: 2, mb: 4 }}>
                    <Typography variant="h4" component="h1" gutterBottom align="center">
                        Create New Task for Project
                    </Typography>
                    <Typography variant="subtitle1" display="block" align="center" color="textSecondary" sx={{mb:2}}>
                    </Typography>

                    {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
                    {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                    {!success && (
                        <Box component="form" onSubmit={handleSubmit} noValidate>
                            <Grid container spacing={2}>
                                <Grid item xs={12}>
                                    <TextField
                                        margin="dense" required fullWidth autoFocus
                                        id="title" label="Task Title" name="title"
                                        value={formData.title} onChange={handleChange}
                                        error={!!fieldErrors.title} helperText={fieldErrors.title}
                                        disabled={loading}
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    <TextField
                                        margin="dense" fullWidth
                                        id="description" label="Description (Optional)" name="description"
                                        multiline rows={4}
                                        value={formData.description} onChange={handleChange}
                                        error={!!fieldErrors.description} helperText={fieldErrors.description}
                                        disabled={loading}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <DateTimePicker
                                        label="Due Date (Optional)"
                                        value={formData.dueDate}
                                        onChange={handleDateChange}
                                        slotProps={{ textField: { fullWidth: true, margin:"dense", variant:"outlined", error:!!fieldErrors.dueDate } }}
                                        disabled={loading}
                                    />
                                    {fieldErrors.dueDate && <Typography color="error" variant="caption" sx={{ml:1}}>{fieldErrors.dueDate}</Typography>}
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <FormControl fullWidth margin="dense" required error={!!fieldErrors.status}>
                                        <InputLabel id="status-label">Status</InputLabel>
                                        <Select
                                            labelId="status-label" name="status"
                                            value={formData.status} label="Status"
                                            onChange={handleChange} disabled={loading}
                                        >
                                            {taskStatusOptions.map(status => (
                                                <MenuItem key={status} value={status}>{status.replace('_', ' ')}</MenuItem>
                                            ))}
                                        </Select>
                                        {fieldErrors.status && <Typography color="error" variant="caption" sx={{ml:1}}>{fieldErrors.status}</Typography>}
                                    </FormControl>
                                </Grid>
                                <Grid item xs={12}>
                                    <FormControl fullWidth margin="dense" disabled={collaborators.length === 0}>
                                        <InputLabel id="assignee-label">Assign to (Optional)</InputLabel>
                                        <Select
                                            labelId="assignee-label" name="assignedUserId"
                                            value={formData.assignedUserId} label="Assign to (Optional)"
                                            onChange={handleChange} disabled={loading}
                                        >
                                            <MenuItem value=""><em>Unassigned</em></MenuItem>
                                            {collaborators.map(collab => (
                                                <MenuItem key={collab.id} value={collab.id}>
                                                    {`${collab.firstName || ''} ${collab.lastName || ''}`.trim() || collab.username}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Grid>
                            </Grid>
                            <Button
                                type="submit" fullWidth variant="contained"
                                sx={{ mt: 3, mb: 2, py: 1.5 }}
                                disabled={loading || !formData.title.trim()}
                            >
                                {loading ? <CircularProgress size={24} color="inherit" /> : 'Create Task'}
                            </Button>
                        </Box>
                    )}
                </Paper>
            </Container>
        </LocalizationProvider>
    );
}

export default CreateTaskPage;