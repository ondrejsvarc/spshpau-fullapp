import React, { useState, useEffect, useCallback } from 'react';
import { Link as RouterLink, useNavigate }
    from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import {
    getMyConnections,
    getMyIncomingRequests,
    getMyOutgoingRequests,
    removeConnection,
    acceptConnectionRequest,
    rejectConnectionRequest,
} from '../services/api';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Divider from '@mui/material/Divider';
import Pagination from '@mui/material/Pagination';
import ChatIcon from '@mui/icons-material/Chat';
import PersonRemoveIcon from '@mui/icons-material/PersonRemove';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';

const UserListSection = ({ title, users, onAction1, action1Label, onAction2, action2Label, action1Icon, action2Icon, onUserClick, loading, actionLoading, noDataMessage = "No users in this list.", sx }) => {
    return (
        <Paper elevation={2} sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column', width: '100%', ...sx }}>
            <Typography variant="h6" gutterBottom>{title}</Typography>
            <Divider sx={{ mb: 1 }}/>
            {loading && users.length === 0 ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexGrow: 1 }}>
                    <CircularProgress />
                </Box>
            ) : users.length === 0 ? (
                <Typography color="textSecondary" sx={{ textAlign: 'center', mt: 2 }}>{noDataMessage}</Typography>
            ) : (
                <List dense sx={{ overflow: 'auto', flexGrow: 1 }}>
                    {users.map((user, index) => (
                        <ListItem
                            key={user.id}
                            divider={index < users.length - 1}
                            secondaryAction={
                                <Box>
                                    {onAction1 && action1Label && (
                                        <IconButton
                                            edge="end"
                                            aria-label={action1Label.toLowerCase()}
                                            onClick={() => onAction1(user.id)}
                                            disabled={actionLoading && actionLoading[user.id]}
                                            size="small"
                                            sx={{mr: onAction2 ? 0.5 : 0 }}
                                        >
                                            {actionLoading && actionLoading[user.id] && onAction1.name.includes(user.id) ? <CircularProgress size={20} /> : action1Icon}
                                        </IconButton>
                                    )}
                                    {onAction2 && action2Label && (
                                        <IconButton
                                            edge="end"
                                            aria-label={action2Label.toLowerCase()}
                                            onClick={() => onAction2(user.id)}
                                            disabled={actionLoading && actionLoading[user.id]}
                                            size="small"
                                        >
                                            {actionLoading && actionLoading[user.id] && onAction2.name.includes(user.id) ? <CircularProgress size={20} /> : action2Icon}
                                        </IconButton>
                                    )}
                                </Box>
                            }
                        >
                            <ListItemAvatar>
                                <Avatar component={RouterLink} to={`/users/${user.id}`}>
                                    {`${user.firstName ? user.firstName[0] : ''}${user.lastName ? user.lastName[0] : ''}`.toUpperCase() || (user.username ? user.username[0].toUpperCase() : 'U')}
                                </Avatar>
                            </ListItemAvatar>
                            <ListItemText
                                primary={
                                    <Typography
                                        component={RouterLink}
                                        to={`/users/${user.id}`}
                                        sx={{ textDecoration: 'none', color: 'inherit', '&:hover': { textDecoration: 'underline' } }}
                                    >
                                        {`${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username}
                                    </Typography>
                                }
                                secondary={`@${user.username || 'username_n/a'}`}
                            />
                        </ListItem>
                    ))}
                </List>
            )}
        </Paper>
    );
};


function ConnectionsPage() {
    const { keycloakAuthenticated } = useUser();
    const navigate = useNavigate();

    const [connections, setConnections] = useState([]);
    const [incomingRequests, setIncomingRequests] = useState([]);
    const [outgoingRequests, setOutgoingRequests] = useState([]);

    const [loadingConnections, setLoadingConnections] = useState(true);
    const [loadingIncoming, setLoadingIncoming] = useState(true);
    const [loadingOutgoing, setLoadingOutgoing] = useState(true);

    const [error, setError] = useState(null);
    const [actionLoading, setActionLoading] = useState({});

    const [connPage, setConnPage] = useState(1);
    const [connTotalPages, setConnTotalPages] = useState(0);
    const [incomingPage, setIncomingPage] = useState(1);
    const [incomingTotalPages, setIncomingTotalPages] = useState(0);
    const [outgoingPage, setOutgoingPage] = useState(1);
    const [outgoingTotalPages, setOutgoingTotalPages] = useState(0);

    const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
    const [userToConfirmAction, setUserToConfirmAction] = useState(null);
    const [confirmActionCallback, setConfirmActionCallback] = useState(null);
    const [confirmDialogText, setConfirmDialogText] = useState({ title: '', message: ''});


    const fetchData = useCallback(async () => {
        if (!keycloakAuthenticated) return;
        setLoadingConnections(true);
        setLoadingIncoming(true);
        setLoadingOutgoing(true);
        setError(null);
        try {
            const [connData, incomingData, outgoingData] = await Promise.all([
                getMyConnections(connPage - 1, 5),
                getMyIncomingRequests(incomingPage - 1, 5),
                getMyOutgoingRequests(outgoingPage - 1, 5)
            ]);

            setConnections(connData.content || []);
            setConnTotalPages(connData.totalPages || 0);

            setIncomingRequests(incomingData.content || []);
            setIncomingTotalPages(incomingData.totalPages || 0);

            setOutgoingRequests(outgoingData.content || []);
            setOutgoingTotalPages(outgoingData.totalPages || 0);

        } catch (err) {
            console.error("Failed to fetch connection data:", err);
            setError(err.message || "Could not load connection data.");
        } finally {
            setLoadingConnections(false);
            setLoadingIncoming(false);
            setLoadingOutgoing(false);
        }
    }, [keycloakAuthenticated, connPage, incomingPage, outgoingPage]);

    useEffect(() => {
        fetchData();
    }, [fetchData]);


    const handleAction = async (actionFn, userId, successMessage) => {
        setActionLoading(prev => ({ ...prev, [userId]: true }));
        try {
            await actionFn(userId);
            console.log(successMessage);
            fetchData();
        } catch (err) {
            console.error(`Action failed for user ${userId}:`, err);
            setError(`Action failed: ${err.message || 'Please try again.'}`);
        } finally {
            setActionLoading(prev => ({ ...prev, [userId]: false }));
            setConfirmDialogOpen(false);
            setUserToConfirmAction(null);
        }
    };

    const openConfirmation = (userId, actionType) => {
        setUserToConfirmAction(userId);
        if (actionType === 'remove') {
            setConfirmDialogText({title: 'Remove Connection?', message: 'Are you sure you want to remove this connection?'});
            setConfirmActionCallback(() => () => handleAction(removeConnection, userId, 'Connection removed.'));
        }
        else if (actionType === 'reject') {
            setConfirmDialogText({title: 'Reject Request?', message: 'Are you sure you want to reject this connection request?'});
            setConfirmActionCallback(() => () => handleAction(rejectConnectionRequest, userId, 'Request rejected.'));
        }
        setConfirmDialogOpen(true);
    };


    const renderPagination = (totalPages, currentPage, handler) => {
        if (totalPages <= 1) return null;
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2, mb:1 }}>
                <Pagination count={totalPages} page={currentPage} onChange={(e, val) => handler(val)} color="primary" size="small"/>
            </Box>
        );
    }


    return (
        <Container maxWidth="lg" sx={{height: 'calc(100vh - 64px - 48px)', display: 'flex', flexDirection: 'column', pt: 2}}>
            <Typography variant="h4" component="h1" gutterBottom sx={{ mt: 2, mb: 3, textAlign: 'center' }}>
                My Connections
            </Typography>
            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <Grid container spacing={3} sx={{ flexGrow: 1, overflow: 'hidden' }}>

                {/* Left Column: Current Connections */}
                <Grid xs={12} md={5} sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                    <UserListSection
                        title="Your Connections"
                        users={connections}
                        loading={loadingConnections}
                        action1Label="Open Chat"
                        onAction1={(userId) => {
                            console.log(`Open chat with ${userId}`);
                            navigate(`/chat/${userId}`);
                        }}
                        action1Icon={<ChatIcon />}
                        action2Label="Remove Connection"
                        onAction2={(userId) => openConfirmation(userId, 'remove')}
                        action2Icon={<PersonRemoveIcon color="error"/>}
                        actionLoading={actionLoading}
                        noDataMessage="You have no connections yet."
                        sx={{ flexGrow: 1, minHeight:0 }}
                    />
                    {renderPagination(connTotalPages, connPage, setConnPage)}
                </Grid>

                {/* Right Column: Incoming and Outgoing Requests */}
                <Grid xs={12} md={7} sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', gap: 2 }}>
                        <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
                            <UserListSection
                                title="Incoming Requests"
                                users={incomingRequests}
                                loading={loadingIncoming}
                                action1Label="Accept"
                                onAction1={(userId) => handleAction(acceptConnectionRequest, userId, 'Request accepted.')}
                                action1Icon={<CheckCircleIcon color="success"/>}
                                action2Label="Reject"
                                onAction2={(userId) => openConfirmation(userId, 'reject')}
                                action2Icon={<CancelIcon color="error"/>}
                                actionLoading={actionLoading}
                                noDataMessage="No incoming connection requests."
                                sx={{ flexGrow: 1, minHeight:0 }}
                            />
                            {renderPagination(incomingTotalPages, incomingPage, setIncomingPage)}
                        </Box>
                        <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
                            <UserListSection
                                title="Outgoing Requests"
                                users={outgoingRequests}
                                loading={loadingOutgoing}
                                noDataMessage="No pending outgoing requests."
                                sx={{ flexGrow: 1, minHeight:0 }}
                            />
                            {renderPagination(outgoingTotalPages, outgoingPage, setOutgoingPage)}
                        </Box>
                    </Box>
                </Grid>
            </Grid>

            <Dialog
                open={confirmDialogOpen}
                onClose={() => setConfirmDialogOpen(false)}
                aria-labelledby="alert-dialog-title"
                aria-describedby="alert-dialog-description"
            >
                <DialogTitle id="alert-dialog-title">{confirmDialogText.title}</DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        {confirmDialogText.message}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setConfirmDialogOpen(false)}>Cancel</Button>
                    <Button
                        onClick={() => {
                            if (userToConfirmAction && confirmActionCallback) {
                                const actionToRun = confirmActionCallback();
                                actionToRun();
                            }
                        }}
                        autoFocus
                        color={confirmDialogText.title.toLowerCase().includes("remove") || confirmDialogText.title.toLowerCase().includes("reject") ? "error" : "primary"}
                    >
                        Confirm
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
}

export default ConnectionsPage;