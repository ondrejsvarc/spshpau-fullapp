import React, { useState, useEffect, useCallback } from 'react';
import { useUser } from '../contexts/UserContext';
import { useNavigate } from 'react-router-dom';
import {
    getMyProducerProfile,
    patchMyProducerProfile,
    getAllGenres,
    addGenreToProducerProfile,
    removeGenreFromProducerProfile
} from '../services/api';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Switch from '@mui/material/Switch';
import FormControlLabel from '@mui/material/FormControlLabel';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import Chip from '@mui/material/Chip';
import Stack from '@mui/material/Stack';
import IconButton from '@mui/material/IconButton';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Checkbox from '@mui/material/Checkbox';
import Divider from '@mui/material/Divider';

const experienceLevels = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];

const EditableField = ({ label, value, type = 'text', multiline = false, rows = 1, options = [], isEditing, onValueChange, onEditToggle, onSave, onCancel, loading }) => {
    if (!isEditing) {
        return (
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', py: 1, minHeight: '56px' }}>
                <Box>
                    <Typography variant="caption" color="textSecondary" display="block">{label}</Typography>
                    <Typography variant="body1">
                        {type === 'boolean' ? (value ? 'Yes' : 'No') : (value || 'N/A')}
                    </Typography>
                </Box>
                <IconButton onClick={onEditToggle} aria-label={`edit ${label.toLowerCase()}`}><EditIcon /></IconButton>
            </Box>
        );
    }
    return (
        <Box sx={{ my: 2 }}>
            <Typography variant="caption" color="textSecondary" display="block" sx={{mb:0.5}}>{label}</Typography>
            {type === 'select' ? (
                <FormControl fullWidth><InputLabel id={`${label}-select-label`}>{label}</InputLabel><Select labelId={`${label}-select-label`} value={value || ''} label={label} onChange={(e) => onValueChange(e.target.value)}>{options.map(opt => <MenuItem key={opt} value={opt}>{opt.charAt(0) + opt.slice(1).toLowerCase()}</MenuItem>)}</Select></FormControl>
            ) : type === 'boolean' ? (
                <FormControlLabel control={<Switch checked={!!value} onChange={(e) => onValueChange(e.target.checked)} />} label={value ? "Yes" : "No"} />
            ) : (
                <TextField fullWidth variant="outlined" value={value || ''} onChange={(e) => onValueChange(e.target.value)} multiline={multiline} rows={rows} autoFocus />
            )}
            <Box sx={{ mt: 1, display: 'flex', gap: 1 }}>
                <Button onClick={onSave} variant="contained" startIcon={loading ? <CircularProgress size={20}/> : <SaveIcon />} disabled={loading}>Save</Button>
                <Button onClick={onCancel} variant="outlined" startIcon={<CancelIcon />}>Cancel</Button>
            </Box>
        </Box>
    );
};

const ItemSelectionModal = ({ open, onClose, title, currentItems, availableItems, onSave, loading }) => {
    const [selected, setSelected] = useState([]);
    useEffect(() => { if (open) { setSelected(currentItems.map(item => item.id)); } }, [open, currentItems]);
    const handleToggle = (value) => { const currentIndex = selected.indexOf(value); const newSelected = [...selected]; if (currentIndex === -1) { newSelected.push(value); } else { newSelected.splice(currentIndex, 1); } setSelected(newSelected); };
    const handleSave = () => { onSave(selected); };
    return ( <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth> <DialogTitle>{title}</DialogTitle> <DialogContent dividers> <List dense> {availableItems.map((item) => ( <ListItem key={item.id} dense button onClick={() => handleToggle(item.id)}> <Checkbox edge="start" checked={selected.indexOf(item.id) !== -1} tabIndex={-1} disableRipple /> <ListItemText primary={item.name} /> </ListItem> ))} {availableItems.length === 0 && <Typography sx={{p:2}}>No available items to select.</Typography>} </List> </DialogContent> <DialogActions> <Button onClick={onClose}>Cancel</Button> <Button onClick={handleSave} variant="contained" disabled={loading}>{loading ? <CircularProgress size={24} /> : "Save Selections"}</Button> </DialogActions> </Dialog> );
};


function ProducerProfilePage() {
    const { appUser, refreshAppUser } = useUser();
    const navigate = useNavigate();

    const [profile, setProfile] = useState(null);
    const [loadingProfile, setLoadingProfile] = useState(true);
    const [profileError, setProfileError] = useState(null);

    const [editField, setEditField] = useState(null);
    const [fieldValue, setFieldValue] = useState('');

    const [allGenres, setAllGenres] = useState([]);
    const [genreModalOpen, setGenreModalOpen] = useState(false);
    const [modalLoading, setModalLoading] = useState(false);

    const fetchProfileData = useCallback(async () => {
        if (!appUser?.id) return;
        setLoadingProfile(true);
        setProfileError(null);
        try {
            const data = await getMyProducerProfile();
            setProfile(data);
        } catch (err) {
            if (err.message.includes("404")) {
                setProfile(null);
            } else {
                setProfileError(err.message || 'Failed to fetch producer profile.');
            }
        } finally {
            setLoadingProfile(false);
        }
    }, [appUser]);

    useEffect(() => {
        if (appUser?.producerProfile) {
            setProfile(appUser.producerProfile);
            setLoadingProfile(false);
        } else if (appUser?.id) {
            fetchProfileData();
        }
    }, [appUser, fetchProfileData]);

    const handleEditToggle = (fieldName, currentValue) => {
        setEditField(fieldName);
        setFieldValue(currentValue);
    };

    const handleCancelEdit = () => {
        setEditField(null);
        setFieldValue('');
    };

    const handleSaveField = async () => {
        if (editField === null) return;
        setLoadingProfile(true);
        try {
            const payload = { [editField]: fieldValue };
            const updatedProfile = await patchMyProducerProfile(payload);
            setProfile(updatedProfile);
            if (refreshAppUser) await refreshAppUser();
            setEditField(null);
        } catch (err) {
            setProfileError(err.message || `Failed to update ${editField}`);
        } finally {
            setLoadingProfile(false);
        }
    };

    const openGenreModal = async () => {
        setModalLoading(true);
        try {
            const genresData = await getAllGenres(0, 100);
            setAllGenres(genresData.content || []);
            setGenreModalOpen(true);
        } catch (err) {
            setProfileError("Failed to load genres: " + err.message);
        } finally {
            setModalLoading(false);
        }
    };

    const handleSaveGenres = async (selectedGenreIds) => {
        setModalLoading(true);
        const currentGenreIds = profile.genres.map(g => g.id);
        const toAdd = selectedGenreIds.filter(id => !currentGenreIds.includes(id));
        const toRemove = currentGenreIds.filter(id => !selectedGenreIds.includes(id));

        try {
            for (const id of toRemove) { await removeGenreFromProducerProfile(id); }
            for (const id of toAdd) { await addGenreToProducerProfile(id); }
            await fetchProfileData();
            if(refreshAppUser) await refreshAppUser();
        } catch (err) {
            setProfileError("Error updating genres: " + err.message);
        } finally {
            setModalLoading(false);
            setGenreModalOpen(false);
        }
    };

    if (loadingProfile && !profile) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 5 }}><CircularProgress /></Box>;
    }

    if (profileError && !profile) {
        return <Container sx={{mt:2}}><Alert severity="error">{profileError}</Alert></Container>;
    }

    if (!profile) {
        return (
            <Container maxWidth="sm" sx={{ textAlign: 'center', mt: 5 }}>
                <Typography variant="h5" gutterBottom>No Producer Profile Found</Typography>
                <Typography sx={{ mb: 2 }}>
                    You haven't created a producer profile yet. Would you like to create one?
                </Typography>
                <Button variant="contained" onClick={() => navigate('/account/producer-profile/create')}>
                    Create Producer Profile
                </Button>
            </Container>
        );
    }

    return (
        <Container maxWidth="md">
            <Typography variant="h4" component="h1" gutterBottom sx={{ mt: 2, mb: 3, textAlign: 'center' }}>
                Manage Producer Profile
            </Typography>
            {profileError && <Alert severity="warning" sx={{mb:2}}>{profileError} (showing cached data if available)</Alert>}

            <Paper elevation={3} sx={{ p: { xs: 2, sm: 3 }, mb: 4 }}>
                <Typography variant="h6" gutterBottom>Profile Details</Typography>
                <EditableField
                    label="Availability"
                    value={editField === 'availability' ? fieldValue : profile.availability}
                    type="boolean"
                    isEditing={editField === 'availability'}
                    onValueChange={(val) => setFieldValue(val)}
                    onEditToggle={() => handleEditToggle('availability', profile.availability)}
                    onSave={handleSaveField}
                    onCancel={handleCancelEdit}
                    loading={loadingProfile && editField === 'availability'}
                />
                <Divider sx={{ my: 1 }} />
                <EditableField
                    label="Bio"
                    value={editField === 'bio' ? fieldValue : profile.bio}
                    multiline
                    rows={4}
                    isEditing={editField === 'bio'}
                    onValueChange={(val) => setFieldValue(val)}
                    onEditToggle={() => handleEditToggle('bio', profile.bio)}
                    onSave={handleSaveField}
                    onCancel={handleCancelEdit}
                    loading={loadingProfile && editField === 'bio'}
                />
                <Divider sx={{ my: 1 }} />
                <EditableField
                    label="Experience Level"
                    value={editField === 'experienceLevel' ? fieldValue : profile.experienceLevel}
                    type="select"
                    options={experienceLevels}
                    isEditing={editField === 'experienceLevel'}
                    onValueChange={(val) => setFieldValue(val)}
                    onEditToggle={() => handleEditToggle('experienceLevel', profile.experienceLevel)}
                    onSave={handleSaveField}
                    onCancel={handleCancelEdit}
                    loading={loadingProfile && editField === 'experienceLevel'}
                />
            </Paper>

            <Paper elevation={3} sx={{ p: { xs: 2, sm: 3 }, mb: 4 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                    <Typography variant="h6">Genres</Typography>
                    <IconButton onClick={openGenreModal} color="primary" aria-label="edit genres" disabled={modalLoading}>
                        {modalLoading ? <CircularProgress size={24} /> : <AddIcon />}
                    </IconButton>
                </Box>
                <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                    {profile.genres && profile.genres.length > 0 ? (
                        profile.genres.map(genre => <Chip key={genre.id} label={genre.name} />)
                    ) : (
                        <Typography variant="body2" color="textSecondary">No genres added yet.</Typography>
                    )}
                </Stack>
            </Paper>

            <ItemSelectionModal
                open={genreModalOpen}
                onClose={() => setGenreModalOpen(false)}
                title="Select Genres for Producer Profile"
                currentItems={profile.genres || []}
                availableItems={allGenres}
                onSave={handleSaveGenres}
                loading={modalLoading}
            />
        </Container>
    );
}

export default ProducerProfilePage;