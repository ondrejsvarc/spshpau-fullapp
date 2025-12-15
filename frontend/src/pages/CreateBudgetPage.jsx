import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import { createProjectBudgetApi } from '../services/api';

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

function CreateBudgetPage() {
    const { projectId } = useParams();
    const navigate = useNavigate();
    const { keycloakAuthenticated } = useUser();

    const [formData, setFormData] = useState({
        totalAmount: '',
        currency: 'USD',
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: name === 'totalAmount' ? (value === '' ? '' : parseFloat(value)) : value.toUpperCase(),
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (!formData.totalAmount || formData.totalAmount <= 0) {
            setError('Total Amount must be a positive number.');
            return;
        }
        if (!formData.currency.trim() || formData.currency.trim().length !== 3) {
            setError('Currency code must be 3 letters (e.g., USD, EUR).');
            return;
        }
        if (!keycloakAuthenticated) {
            setError('You must be logged in to create a budget.');
            return;
        }

        setLoading(true);
        setError(null);
        setSuccess(null);

        try {
            const budgetDataPayload = {
                totalAmount: parseFloat(formData.totalAmount),
                currency: formData.currency.trim().toUpperCase(),
            };

            console.log('Creating project budget with payload:', budgetDataPayload);
            await createProjectBudgetApi(projectId, budgetDataPayload);
            setSuccess(`Budget created successfully! Redirecting back to project...`);

            setTimeout(() => {
                navigate(`/projects/${projectId}`);
            }, 1500);

        } catch (err) {
            console.error("Failed to create project budget:", err);
            let errorMessage = 'Failed to create budget. Please try again.';
            if (err.message && err.message.includes("API Error")) {
                // Basic parsing, can be improved
                const apiErrorMsg = err.message.split('API Error ')[1]?.split(': ')[1];
                errorMessage = apiErrorMsg || errorMessage;
                if (err.message.includes("409")) {
                    errorMessage = "A budget already exists for this project. You can manage it on the project page.";
                }
            } else if (err.message) {
                errorMessage = err.message;
            }
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="sm">
            <Button
                startIcon={<ArrowBackIcon />}
                onClick={() => navigate(`/projects/${projectId}`)}
                sx={{ mt: 2, mb: 1 }}
            >
                Back to Project
            </Button>
            <Paper elevation={3} sx={{ p: { xs: 2, sm: 4 }, mt: 2, mb: 4 }}>
                <Typography variant="h4" component="h1" gutterBottom align="center">
                    Create Budget for Project
                </Typography>
                <Typography variant="caption" display="block" align="center" color="textSecondary" sx={{mb:2}}>
                    Project ID: {projectId}
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
                                    id="totalAmount"
                                    label="Total Budget Amount"
                                    name="totalAmount"
                                    type="number"
                                    value={formData.totalAmount}
                                    onChange={handleChange}
                                    autoFocus
                                    error={!!error && (!formData.totalAmount || formData.totalAmount <= 0)}
                                    helperText={!!error && (!formData.totalAmount || formData.totalAmount <= 0) ? "Amount must be positive" : ""}
                                    disabled={loading}
                                    InputProps={{
                                        inputProps: {
                                            step: "0.01",
                                            min: "0.01"
                                        }
                                    }}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    margin="dense"
                                    required
                                    fullWidth
                                    id="currency"
                                    label="Currency Code (e.g., USD, EUR)"
                                    name="currency"
                                    value={formData.currency}
                                    onChange={handleChange}
                                    error={!!error && (!formData.currency.trim() || formData.currency.trim().length !== 3)}
                                    helperText={!!error && (!formData.currency.trim() || formData.currency.trim().length !== 3) ? "Must be 3 letters" : ""}
                                    disabled={loading}
                                    inputProps={{
                                        maxLength: 3,
                                        style: { textTransform: 'uppercase' }
                                    }}
                                />
                            </Grid>
                        </Grid>

                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            sx={{ mt: 3, mb: 2, py: 1.5 }}
                            disabled={loading || !formData.totalAmount || formData.totalAmount <= 0 || !formData.currency.trim() || formData.currency.trim().length !== 3}
                        >
                            {loading ? <CircularProgress size={24} color="inherit" /> : 'Create Budget'}
                        </Button>
                    </Box>
                )}
            </Paper>
        </Container>
    );
}

export default CreateBudgetPage;