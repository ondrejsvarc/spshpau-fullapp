import React, { useState, useEffect } from 'react';
import { useUser } from '../contexts/UserContext';
import { useNavigate } from 'react-router-dom';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import PersonIcon from '@mui/icons-material/Person';
import MusicNoteIcon from '@mui/icons-material/MusicNote';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import IconButton from '@mui/material/IconButton';
import CancelIcon from '@mui/icons-material/Cancel';

import { updateUserLocation } from '../services/api';

const ProfileTile = ({ title, icon, hasProfile, onCreate, onManage }) => {
    return (
        <Paper
            elevation={3}
            sx={{
                p: 3,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'space-between',
                height: '100%',
                minHeight: 180,
            }}
        >
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                {icon}
                <Typography variant="h6" component="h3" sx={{ ml: 1 }}>
                    {title}
                </Typography>
            </Box>
            {hasProfile ? (
                <Button variant="outlined" startIcon={<EditIcon />} onClick={onManage}>
                    Manage Profile
                </Button>
            ) : (
                <Button variant="contained" startIcon={<AddCircleOutlineIcon />} onClick={onCreate}>
                    Create Profile
                </Button>
            )}
        </Paper>
    );
};

const InfoDisplay = ({ label, value }) => (
    <Box sx={{ mb: 2 }}>
        <Typography variant="caption" color="textSecondary" display="block">
            {label}
        </Typography>
        <Typography variant="body1">
            {value || 'N/A'}
        </Typography>
    </Box>
);

function AccountPage() {
    const { appUser, loadingUser, userError, keycloakAuthenticated, keycloakInitialized, refreshAppUser } = useUser();
    const navigate = useNavigate();
    const [currentLocation, setCurrentLocation] = useState('');
    const [editLocation, setEditLocation] = useState('');
    const [isEditingLocation, setIsEditingLocation] = useState(false);
    const [locationEditStatus, setLocationEditStatus] = useState({ loading: false, error: null, success: null });

    useEffect(() => {
        if (appUser) {
            setCurrentLocation(appUser.location || '');
            setEditLocation(appUser.location || '');
        }
    }, [appUser]);

    const handleEditLocationToggle = () => {
        if (!isEditingLocation) {setEditLocation(currentLocation);
        }
        setIsEditingLocation(!isEditingLocation);
        setLocationEditStatus({ loading: false, error: null, success: null });
    };

    const handleLocationInputChange = (event) => {
        setEditLocation(event.target.value);
    };

    const handleSaveLocation = async () => {
        setLocationEditStatus({ loading: true, error: null, success: null });
        try {
            await updateUserLocation({ location: editLocation });
            setLocationEditStatus({ loading: false, error: null, success: 'Location updated successfully!' });
            setCurrentLocation(editLocation);
            setIsEditingLocation(false);
            if(refreshAppUser) refreshAppUser();
        } catch (error) {
            setLocationEditStatus({ loading: false, error: error.message || 'Failed to update location.', success: null });
        }
    };

    if (!keycloakInitialized || loadingUser && !appUser) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
                <CircularProgress />
            </Box>
        );
    }

    if (userError && !appUser) {
        return <Container sx={{mt:2}}><Alert severity="error">Error loading user data: {userError}</Alert></Container>;
    }

    if (!keycloakAuthenticated || !appUser) {
        return <Container sx={{mt:2}}><Typography>Please log in to view your account.</Typography></Container>;
    }

    return (
        <Container maxWidth="md">
            <Typography variant="h4" component="h1" gutterBottom sx={{ mt: 2, mb: 3, textAlign: 'center' }}>
                My Account
            </Typography>

            <Paper elevation={3} sx={{ p: { xs: 2, sm: 3 }, mb: 4 }}>
                <Typography variant="h6" gutterBottom>Personal Information</Typography>
                <Grid container spacing={2}>
                    <Grid item xs={12} sm={6}>
                        <InfoDisplay label="Username" value={appUser.username} />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                        <InfoDisplay label="Email" value={appUser.email} />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                        <InfoDisplay label="First Name" value={appUser.firstName} />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                        <InfoDisplay label="Last Name" value={appUser.lastName} />
                    </Grid>
                    <Grid item xs={12}>
                        <Typography variant="caption" color="textSecondary" display="block">
                            Location
                        </Typography>
                        {!isEditingLocation ? (
                            <Box sx={{ display: 'flex', alignItems: 'center', minHeight: '56px'}}>
                                <Typography variant="body1" sx={{ flexGrow: 1 }}>
                                    {currentLocation || 'N/A'}
                                </Typography>
                                <IconButton onClick={handleEditLocationToggle} aria-label="edit location">
                                    <EditIcon />
                                </IconButton>
                            </Box>
                        ) : (
                            <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1, mt: 0.5 }}>
                                <TextField
                                    label="Location"
                                    value={editLocation}
                                    onChange={handleLocationInputChange}
                                    fullWidth
                                    margin="dense"
                                    variant="outlined"
                                    autoFocus
                                />
                                <Button
                                    variant="contained"
                                    onClick={handleSaveLocation}
                                    disabled={locationEditStatus.loading}
                                    startIcon={locationEditStatus.loading ? <CircularProgress size={20} /> : <SaveIcon />}
                                    sx={{ height: '56px', mt: '8px' }}
                                >
                                    Save
                                </Button>
                                <IconButton onClick={handleEditLocationToggle} sx={{ mt: '8px' }} aria-label="cancel editing location">
                                    <CancelIcon />
                                </IconButton>
                            </Box>
                        )}
                        {locationEditStatus.error && <Alert severity="error" sx={{ mt: 1 }}>{locationEditStatus.error}</Alert>}
                        {locationEditStatus.success && <Alert severity="success" sx={{ mt: 1, mb:1 }}>{locationEditStatus.success}</Alert>}
                    </Grid>
                </Grid>
            </Paper>

            <Typography variant="h5" component="h2" gutterBottom sx={{ mb: 3, textAlign: 'center' }}>
                My Profiles
            </Typography>
            <Grid container spacing={3} justifyContent="center">
                <Grid item xs={12} sm={6} md={5}>
                    <ProfileTile
                        title="Artist Profile"
                        icon={<PersonIcon sx={{ fontSize: 40 }} color="primary" />}
                        hasProfile={!!appUser.artistProfile}
                        onCreate={() => navigate('/account/artist-profile/create')}
                        onManage={() => navigate('/account/artist-profile')}
                    />
                </Grid>
                <Grid item xs={12} sm={6} md={5}>
                    <ProfileTile
                        title="Producer Profile"
                        icon={<MusicNoteIcon sx={{ fontSize: 40 }} color="secondary" />}
                        hasProfile={!!appUser.producerProfile}
                        onCreate={() => navigate('/account/producer-profile/create')}
                        onManage={() => navigate('/account/producer-profile')}
                    />
                </Grid>
            </Grid>
        </Container>
    );
}

export default AccountPage;