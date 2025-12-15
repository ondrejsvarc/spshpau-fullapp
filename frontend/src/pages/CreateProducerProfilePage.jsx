import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import { createOrUpdateProducerProfile } from '../services/api';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';

const experienceLevels = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];

function CreateProducerProfilePage() {
    const navigate = useNavigate();
    const { appUser, refreshAppUser } = useUser();

    const [formData, setFormData] = useState({
        experienceLevel: '',
        bio: '',
        availability: false,
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleChange = (event) => {
        const { name, value, type, checked } = event.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: type === 'checkbox' ? checked : value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (!formData.experienceLevel) {
            setError('Experience Level is required.');
            return;
        }
        setLoading(true);
        setError(null);
        try {
            await createOrUpdateProducerProfile(formData);
            if (refreshAppUser) {
                await refreshAppUser();
            }
            navigate('/account/producer-profile');
        } catch (err) {
            console.error("Failed to create producer profile:", err);
            setError(err.message || 'Failed to create profile. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    if (!appUser) {
        return (
            <Container>
                <Typography sx={{mt:2}}>Loading user data or user not logged in...</Typography>
            </Container>
        );
    }

    return (
        <Container maxWidth="sm">
            <Paper elevation={3} sx={{ p: { xs: 2, sm: 4 }, mt: 4 }}>
                <Typography variant="h4" component="h1" gutterBottom align="center">
                    Create Producer Profile
                </Typography>
                <Typography variant="body2" color="textSecondary" align="center" sx={{mb: 2}}>
                    Select your experience level. Other details can be added now or later.
                </Typography>
                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                <Box component="form" onSubmit={handleSubmit} noValidate>
                    <FormControl fullWidth margin="normal" required>
                        <InputLabel id="experienceLevel-label">Experience Level</InputLabel>
                        <Select
                            labelId="experienceLevel-label"
                            id="experienceLevel"
                            name="experienceLevel"
                            value={formData.experienceLevel}
                            label="Experience Level"
                            onChange={handleChange}
                            required
                        >
                            {experienceLevels.map((level) => (
                                <MenuItem key={level} value={level}>
                                    {level.charAt(0) + level.slice(1).toLowerCase()}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <TextField
                        margin="normal"
                        fullWidth
                        id="bio"
                        label="Bio (Optional)"
                        name="bio"
                        multiline
                        rows={4}
                        value={formData.bio}
                        onChange={handleChange}
                    />

                    <FormControlLabel
                        control={
                            <Switch
                                checked={formData.availability}
                                onChange={handleChange}
                                name="availability"
                                color="primary"
                            />
                        }
                        label="Available for Collaboration"
                        sx={{ mt: 1, mb: 2 }}
                    />

                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        sx={{ mt: 3, mb: 2 }}
                        disabled={loading || !formData.experienceLevel}
                    >
                        {loading ? <CircularProgress size={24} /> : 'Create Profile'}
                    </Button>
                </Box>
            </Paper>
        </Container>
    );
}

export default CreateProducerProfilePage;