import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import {
    getProjectTaskByIdApi,
    updateProjectTaskApi,
    deleteProjectTaskApi,
    getProjectCollaborators,
    assignUserToTaskApi,
    removeUserFromTaskApi
} from '../services/api';

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
import SaveIcon from '@mui/icons-material/Save';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { isValid, formatISO, parseISO } from 'date-fns';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';
import {AdapterDateFns} from "@mui/x-date-pickers/AdapterDateFns";

const taskStatusOptions = ['TODO', 'IN_PROGRESS', 'DONE', 'WAITING', 'REVIEW', 'BLOCKED'];

function TaskEditPage() {
    const { projectId, taskId } = useParams();
    const navigate = useNavigate();
    const { keycloakAuthenticated, appUser } = useUser();

    const [formData, setFormData] = useState({
        title: '',
        description: '',
        dueDate: null,
        status: 'TODO',
        assignedUserId: '',
    });
    const [initialLoading, setInitialLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false);
    const [formError, setFormError] = useState(null);
    const [fieldErrors, setFieldErrors] = useState({});
    const [success, setSuccess] = useState(null);
    const [collaborators, setCollaborators] = useState([]);
    const [confirmDeleteDialogOpen, setConfirmDeleteDialogOpen] = useState(false);

    // Fetch task details and collaborators
    const loadTaskAndCollaborators = useCallback(async () => {
        if (!keycloakAuthenticated || !projectId || !taskId) return;
        setInitialLoading(true);
        setFormError(null);
        try {
            const [taskData, collaboratorsData] = await Promise.all([
                getProjectTaskByIdApi(projectId, taskId),
                getProjectCollaborators(projectId, 0, 100)
            ]);

            setFormData({
                title: taskData.title || '',
                description: taskData.description || '',
                dueDate: taskData.dueDate ? parseISO(taskData.dueDate) : null,
                status: taskData.status || 'TODO',
                assignedUserId: taskData.assignedUser?.id || '',
            });
            setCollaborators(collaboratorsData.content || []);

        } catch (err) {
            console.error("Failed to load task or collaborators:", err);
            setFormError(err.message || 'Could not load task data.');
        } finally {
            setInitialLoading(false);
        }
    }, [projectId, taskId, keycloakAuthenticated]);

    useEffect(() => {
        loadTaskAndCollaborators();
    }, [loadTaskAndCollaborators]);


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
            setFormError('You must be logged in to update a task.');
            return;
        }

        setActionLoading(true);
        setFormError(null);
        setSuccess(null);

        try {
            const taskUpdatePayload = {
                title: formData.title.trim(),
                description: formData.description.trim() || null,
                status: formData.status,
                dueDate: formData.dueDate && isValid(formData.dueDate) ? formatISO(formData.dueDate) : null,
                assignedUserId: formData.assignedUserId || null,
            };

            console.log('Updating task with payload:', taskUpdatePayload);
            const updatedTask = await updateProjectTaskApi(projectId, taskId, taskUpdatePayload);
            setSuccess(`Task "${updatedTask.title}" updated! Redirecting...`);
            setTimeout(() => {
                navigate(`/projects/${projectId}`);
            }, 1500);

        } catch (err) {
            console.error("Failed to update task:", err);
            setFormError(err.message || 'Failed to update task.');
        } finally {
            setActionLoading(false);
        }
    };

    const handleDeleteTask = async () => {
        setConfirmDeleteDialogOpen(false);
        setActionLoading(true);
        setFormError(null);
        try {
            await deleteProjectTaskApi(projectId, taskId);
            setSuccess('Task deleted successfully! Redirecting...');
            setTimeout(() => {
                navigate(`/projects/${projectId}`);
            }, 1500);
        } catch (err) {
            console.error("Failed to delete task:", err);
            setFormError(err.message || 'Failed to delete task.');
            setActionLoading(false);
        }
    };

    if (initialLoading) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}><CircularProgress /></Box>;
    }
    if (formError && !formData.title && initialLoading === false) {
        return <Container sx={{mt:2}}><Alert severity="error">{formError}</Alert></Container>;
    }

    return (
        <LocalizationProvider dateAdapter={AdapterDateFns}>
            <Container maxWidth="md">
                <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(`/projects/${projectId}`)} sx={{ mt: 2, mb: 1 }}>
                    Back to Project
                </Button>
                <Paper elevation={3} sx={{ p: { xs: 2, sm: 4 }, mt: 2, mb: 4 }}>
                    <Typography variant="h4" component="h1" gutterBottom align="center">
                        Edit Task
                    </Typography>
                    <Typography variant="caption" display="block" align="center" color="textSecondary" sx={{mb:2}}>
                        Project ID: {projectId} | Task ID: {taskId}
                    </Typography>

                    {formError && !success && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
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
                                        disabled={actionLoading}
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    <TextField
                                        margin="dense" fullWidth
                                        id="description" label="Description" name="description"
                                        multiline rows={4}
                                        value={formData.description} onChange={handleChange}
                                        error={!!fieldErrors.description} helperText={fieldErrors.description}
                                        disabled={actionLoading}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <DateTimePicker
                                        label="Due Date"
                                        value={formData.dueDate}
                                        onChange={handleDateChange}
                                        slotProps={{ textField: { fullWidth: true, margin:"dense", variant:"outlined", error:!!fieldErrors.dueDate } }}
                                        disabled={actionLoading}
                                    />
                                    {fieldErrors.dueDate && <Typography color="error" variant="caption" sx={{ml:1}}>{fieldErrors.dueDate}</Typography>}
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <FormControl fullWidth margin="dense" required error={!!fieldErrors.status}>
                                        <InputLabel id="status-label">Status</InputLabel>
                                        <Select
                                            labelId="status-label" name="status"
                                            value={formData.status} label="Status"
                                            onChange={handleChange} disabled={actionLoading}
                                        >
                                            {taskStatusOptions.map(statusVal => (
                                                <MenuItem key={statusVal} value={statusVal}>{statusVal.replace('_', ' ')}</MenuItem>
                                            ))}
                                        </Select>
                                        {fieldErrors.status && <Typography color="error" variant="caption" sx={{ml:1}}>{fieldErrors.status}</Typography>}
                                    </FormControl>
                                </Grid>
                                <Grid item xs={12}>
                                    <FormControl fullWidth margin="dense" disabled={collaborators.length === 0 || actionLoading}>
                                        <InputLabel id="assignee-label">Assign to</InputLabel>
                                        <Select
                                            labelId="assignee-label" name="assignedUserId"
                                            value={formData.assignedUserId} label="Assign to"
                                            onChange={handleChange}
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
                            <Box sx={{display: 'flex', justifyContent: 'space-between', mt: 3, mb: 2, flexWrap:'wrap', gap:1}}>
                                <Button
                                    variant="outlined"
                                    color="error"
                                    onClick={() => setConfirmDeleteDialogOpen(true)}
                                    disabled={actionLoading}
                                    startIcon={<DeleteIcon/>}
                                >
                                    Delete Task
                                </Button>
                                <Button
                                    type="submit"
                                    variant="contained"
                                    disabled={actionLoading || !formData.title?.trim()}
                                    startIcon={actionLoading ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />}
                                >
                                    Save Changes
                                </Button>
                            </Box>
                        </Box>
                    )}
                </Paper>

                {/* Delete Confirmation Dialog */}
                <Dialog open={confirmDeleteDialogOpen} onClose={() => setConfirmDeleteDialogOpen(false)}>
                    <DialogTitle>Delete Task?</DialogTitle>
                    <DialogContent><DialogContentText>Are you sure you want to delete this task: "{formData.title || 'this task'}"?</DialogContentText></DialogContent>
                    <DialogActions>
                        <Button onClick={() => setConfirmDeleteDialogOpen(false)}>Cancel</Button>
                        <Button onClick={handleDeleteTask} color="error" autoFocus disabled={actionLoading}>
                            {actionLoading ? <CircularProgress size={20}/> : 'Delete'}
                        </Button>
                    </DialogActions>
                </Dialog>
            </Container>
        </LocalizationProvider>
    );
}

export default TaskEditPage;