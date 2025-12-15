import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import { createProject } from '../services/api';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Grid from '@mui/material/Grid';

function CreateProjectPage() {
    const navigate = useNavigate();
    const { keycloakAuthenticated } = useUser();

    const [formData, setFormData] = useState({
        title: '',
        description: '',
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (!formData.title.trim()) {
            setError('Project Title is required.');
            return;
        }
        if (!keycloakAuthenticated) {
            setError('You must be logged in to create a project.');
            return;
        }

        setLoading(true);
        setError(null);
        setSuccess(null);

        try {
            const projectDataPayload = {
                title: formData.title.trim(),
                description: formData.description.trim() || null,
            };

            const createdProject = await createProject(projectDataPayload);
            console.log('Project created successfully:', createdProject);
            setSuccess(`Project "${createdProject.title}" created successfully! Redirecting...`);

            setTimeout(() => {
                navigate(`/projects/${createdProject.id}`);
            }, 1500);

        } catch (err) {
            console.error("Failed to create project:", err);
            setError(err.message || 'Failed to create project. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="sm">
            <Paper elevation={3} sx={{ p: { xs: 2, sm: 4 }, mt: 4 }}>
                <Typography variant="h4" component="h1" gutterBottom align="center">
                    Create New Project
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                <Box component="form" onSubmit={handleSubmit} noValidate>
                    <Grid container spacing={2}>
                        <Grid item xs={12}>
                            <TextField
                                margin="normal"
                                required
                                fullWidth
                                id="title"
                                label="Project Title"
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
                                margin="normal"
                                fullWidth
                                id="description"
                                label="Project Description (Optional)"
                                name="description"
                                multiline
                                rows={4}
                                value={formData.description}
                                onChange={handleChange}
                                disabled={loading}
                            />
                        </Grid>
                    </Grid>

                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        sx={{ mt: 3, mb: 2 }}
                        disabled={loading || !formData.title.trim()}
                    >
                        {loading ? <CircularProgress size={24} color="inherit" /> : 'Create Project'}
                    </Button>
                </Box>
            </Paper>
        </Container>
    );
}

export default CreateProjectPage;