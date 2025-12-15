import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import {
    getUserSummaryById,
    getArtistProfileByUsername,
    getProducerProfileByUsername,
    getInteractionStatusWithUser,
    sendConnectionRequest,
    removeConnection,
    blockUser,
    unblockUser,
    acceptConnectionRequest,
    rejectConnectionRequest,
} from '../services/api';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Grid from '@mui/material/Grid';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Chip from '@mui/material/Chip';
import Stack from '@mui/material/Stack';
import Avatar from '@mui/material/Avatar';
import Divider from '@mui/material/Divider';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';

// Icons
import PersonIcon from '@mui/icons-material/Person';
import MusicNoteIcon from '@mui/icons-material/MusicNote';
import ChatIcon from '@mui/icons-material/Chat';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import PersonRemoveIcon from '@mui/icons-material/PersonRemove';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import CancelIcon from '@mui/icons-material/Cancel';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';


const ProfileSection = ({ title, profileData, type }) => {
    if (!profileData) {
        return (
            <Paper elevation={2} sx={{ p: 2, mt: 2 }}>
                <Typography variant="h6" gutterBottom>{title}</Typography>
                <Typography color="textSecondary">This user does not have a {type} profile.</Typography>
            </Paper>
        );
    }
    return (
        <Paper elevation={2} sx={{ p: 2, mt: 2 }}>
            <Typography variant="h6" gutterBottom>{title}</Typography>
            <Typography variant="body2"><strong>Availability:</strong> {profileData.availability ? 'Available' : 'Not Available'}</Typography>
            <Typography variant="body2"><strong>Experience:</strong> {profileData.experienceLevel || 'N/A'}</Typography>
            <Typography variant="body2" sx={{ mt: 1 }}><strong>Bio:</strong> {profileData.bio || 'N/A'}</Typography>
            {profileData.genres && profileData.genres.length > 0 && (
                <Box sx={{ mt: 1 }}>
                    <Typography variant="body2"><strong>Genres:</strong></Typography>
                    <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap" sx={{mt:0.5}}>
                        {profileData.genres.map(g => <Chip key={g.id || g.name} label={g.name} size="small" />)}
                    </Stack>
                </Box>
            )}
            {type === 'Artist' && profileData.skills && profileData.skills.length > 0 && (
                <Box sx={{ mt: 1 }}>
                    <Typography variant="body2"><strong>Skills:</strong></Typography>
                    <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap" sx={{mt:0.5}}>
                        {profileData.skills.map(s => <Chip key={s.id || s.name} label={s.name} size="small" />)}
                    </Stack>
                </Box>
            )}
        </Paper>
    );
};


function UserProfilePage() {
    const { userId: targetUserId } = useParams();
    const { appUser: loggedInUser, keycloakAuthenticated } = useUser();
    const navigate = useNavigate();

    const [targetUser, setTargetUser] = useState(null);
    const [artistProfile, setArtistProfile] = useState(null);
    const [producerProfile, setProducerProfile] = useState(null);
    const [interactionStatus, setInteractionStatus] = useState(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [actionLoading, setActionLoading] = useState(false);

    const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
    const [confirmActionDetails, setConfirmActionDetails] = useState({ title: '', message: '', actionFn: null });

    const isOwnProfile = loggedInUser?.id === targetUserId;

    const fetchData = useCallback(async () => {
        if (!targetUserId || !loggedInUser?.id) return;

        setLoading(true);
        setError(null);
        try {
            const summary = await getUserSummaryById(targetUserId);
            setTargetUser(summary);

            if (summary && summary.username) {
                const [artistData, producerData, statusData] = await Promise.allSettled([
                    getArtistProfileByUsername(summary.username).catch(() => null),
                    getProducerProfileByUsername(summary.username).catch(() => null),
                    isOwnProfile ? Promise.resolve(null) : getInteractionStatusWithUser(targetUserId)
                ]);

                setArtistProfile(artistData.status === 'fulfilled' ? artistData.value : null);
                setProducerProfile(producerData.status === 'fulfilled' ? producerData.value : null);
                setInteractionStatus(statusData.status === 'fulfilled' ? statusData.value : null);
            } else if (summary) {
                if (!isOwnProfile) {
                    const statusData = await getInteractionStatusWithUser(targetUserId);
                    setInteractionStatus(statusData);
                }
            } else {
                throw new Error("User not found.");
            }
        } catch (err) {
            console.error("Failed to fetch user profile data:", err);
            setError(err.message || "Could not load user profile.");
        } finally {
            setLoading(false);
        }
    }, [targetUserId, loggedInUser?.id, isOwnProfile]);

    useEffect(() => {
        if (keycloakAuthenticated) {
            fetchData();
        }
    }, [fetchData, keycloakAuthenticated]);

    const handleAction = async (actionFn) => {
        setActionLoading(true);
        setError(null);
        try {
            await actionFn();
            fetchData();
        } catch (err) {
            console.error("Action failed:", err);
            setError(err.message || "Action failed. Please try again.");
        } finally {
            setActionLoading(false);
            setConfirmDialogOpen(false);
        }
    };

    const openConfirmDialog = (title, message, actionFn) => {
        setConfirmActionDetails({ title, message, actionFn });
        setConfirmDialogOpen(true);
    };

    const renderActionButtons = () => {
        if (isOwnProfile || !interactionStatus) return null;

        switch (interactionStatus) {
            case 'CONNECTION_ACCEPTED':
                return (
                    <>
                        <Button variant="outlined" startIcon={<ChatIcon />} sx={{ mr: 1 }} onClick={() => navigate(`/chat/${targetUserId}`)}>Open Chat</Button>
                        <Button variant="outlined" color="error" startIcon={<PersonRemoveIcon />} onClick={() => openConfirmDialog('Remove Connection?', `Are you sure you want to remove ${targetUser?.username} from your connections?`, () => handleAction(() => removeConnection(targetUserId)))}>
                            Remove Connection
                        </Button>
                    </>
                );
            case 'PENDING_OUTGOING':
                return <Button variant="outlined" disabled startIcon={<HourglassEmptyIcon />}>Connection Request Sent</Button>;
            case 'PENDING_INCOMING':
                return (
                    <>
                        <Button variant="contained" color="success" startIcon={<CheckCircleOutlineIcon />} sx={{ mr: 1 }} onClick={() => handleAction(() => acceptConnectionRequest(targetUserId))}>Accept Request</Button>
                        <Button variant="outlined" color="error" startIcon={<CancelIcon />} onClick={() => handleAction(() => rejectConnectionRequest(targetUserId))}>Reject Request</Button>
                    </>
                );
            case 'BLOCKED_BY_YOU':
                return <Button variant="contained" color="warning" startIcon={<BlockIcon />} onClick={() => handleAction(() => unblockUser(targetUserId))}>Unblock User</Button>;
            case 'BLOCKED_BY_OTHER':
            case 'BLOCKED_MUTUAL':
                return <Typography color="error.main">Interaction blocked with this user.</Typography>;
            case 'NONE':
            default:
                return (
                    <>
                        <Button variant="contained" startIcon={<PersonAddIcon />} sx={{ mr: 1 }} onClick={() => handleAction(() => sendConnectionRequest(targetUserId))}>Send Connection Request</Button>
                        <Button variant="outlined" color="error" startIcon={<BlockIcon />} onClick={() => openConfirmDialog('Block User?', `Are you sure you want to block ${targetUser?.username}? This action cannot be easily undone by you.`, () => handleAction(() => blockUser(targetUserId)))}>
                            Block User
                        </Button>
                    </>
                );
        }
    };


    if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}><CircularProgress /></Box>;
    if (error) return <Container sx={{mt:3}}><Alert severity="error">{error}</Alert></Container>;
    if (!targetUser) return <Container sx={{mt:3}}><Typography>User not found.</Typography></Container>;

    return (
        <Container maxWidth="lg" sx={{ py: 3 }}>
            <Paper elevation={3} sx={{ p: { xs: 2, sm: 3 } }}>
                <Grid container spacing={3}>
                    <Grid item xs={12} md={4} sx={{textAlign: {xs: 'center', md: 'left'}}}>
                        <Avatar sx={{ width: 120, height: 120, mb: 2, mx: {xs: 'auto', md: 0} }}>
                            <Typography variant="h2">
                                {`${targetUser.firstName ? targetUser.firstName[0] : ''}${targetUser.lastName ? targetUser.lastName[0] : ''}`.toUpperCase() || (targetUser.username ? targetUser.username[0].toUpperCase() : 'U')}
                            </Typography>
                        </Avatar>
                        <Typography variant="h5" component="h1" gutterBottom>
                            {`${targetUser.firstName || ''} ${targetUser.lastName || ''}`.trim() || targetUser.username}
                        </Typography>
                        <Typography color="textSecondary">@{targetUser.username}</Typography>
                        <Typography color="textSecondary">Location: {targetUser.location || 'N/A'}</Typography>
                    </Grid>

                    <Grid item xs={12} md={8}>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                            {!isOwnProfile && (
                                <Box sx={{ mb: 2, p:1, border: '1px dashed grey', borderRadius: 1, display: 'flex', justifyContent:'center', gap:1, flexWrap:'wrap' }}>
                                    {actionLoading ? <CircularProgress size={24} /> : renderActionButtons()}
                                </Box>
                            )}

                            <ProfileSection title="Artist Profile" profileData={artistProfile} type="Artist" />
                            <ProfileSection title="Producer Profile" profileData={producerProfile} type="Producer" />
                        </Box>
                    </Grid>
                </Grid>
            </Paper>

            <Dialog
                open={confirmDialogOpen}
                onClose={() => setConfirmDialogOpen(false)}
            >
                <DialogTitle>{confirmActionDetails.title}</DialogTitle>
                <DialogContent>
                    <DialogContentText>{confirmActionDetails.message}</DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setConfirmDialogOpen(false)}>Cancel</Button>
                    <Button onClick={confirmActionDetails.actionFn} color="primary" autoFocus>
                        Confirm
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
}

export default UserProfilePage;