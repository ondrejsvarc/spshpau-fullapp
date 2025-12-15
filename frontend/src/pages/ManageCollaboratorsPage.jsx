import React, {useState, useEffect, useCallback, Fragment} from 'react';
import { useParams, useNavigate, Link as RouterLink } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import {
    getProjectById,
    getProjectCollaborators,
    addProjectCollaboratorApi,
    removeProjectCollaboratorApi,
    getAllMyConnections
} from '../services/api';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Avatar from '@mui/material/Avatar';
import ListItemText from '@mui/material/ListItemText';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Divider from '@mui/material/Divider';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import Checkbox from '@mui/material/Checkbox';
import Pagination from '@mui/material/Pagination';
import Tooltip from '@mui/material/Tooltip';

// Icons
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import DeleteIcon from '@mui/icons-material/Delete';
import GroupRemoveIcon from '@mui/icons-material/GroupRemove';
import DialogContentText from "@mui/material/DialogContentText";


function ManageCollaboratorsPage() {
    const { projectId } = useParams();
    const navigate = useNavigate();
    const { appUser, keycloakAuthenticated } = useUser();

    const [project, setProject] = useState(null);
    const [collaborators, setCollaborators] = useState([]);
    const [potentialCollaborators, setPotentialCollaborators] = useState([]);
    const [loadingProject, setLoadingProject] = useState(true);
    const [loadingCollaborators, setLoadingCollaborators] = useState(true);
    const [loadingConnections, setLoadingConnections] = useState(false);
    const [error, setError] = useState(null);
    const [actionLoading, setActionLoading] = useState({});

    const [collabPage, setCollabPage] = useState(1);
    const [collabTotalPages, setCollabTotalPages] = useState(0);
    const collabPageSize = 10;

    const [addModalOpen, setAddModalOpen] = useState(false);
    const [selectedConnectionsToAdd, setSelectedConnectionsToAdd] = useState([]);

    const [confirmRemoveDialogOpen, setConfirmRemoveDialogOpen] = useState(false);
    const [userToRemove, setUserToRemove] = useState(null);

    const isOwner = appUser?.id === project?.owner?.id;

    const fetchProjectDetails = useCallback(async () => {
        if (!keycloakAuthenticated || !projectId) return;
        setLoadingProject(true);
        try {
            const projectData = await getProjectById(projectId);
            setProject(projectData);
        } catch (err) {
            console.error("Failed to fetch project details:", err);
            setError(err.message || "Could not load project details.");
        } finally {
            setLoadingProject(false);
        }
    }, [projectId, keycloakAuthenticated]);

    const fetchCollaboratorsList = useCallback(async (currentPage) => {
        if (!keycloakAuthenticated || !projectId) return;
        setLoadingCollaborators(true);
        try {
            const data = await getProjectCollaborators(projectId, currentPage - 1, collabPageSize);
            setCollaborators(data.content || []);
            setCollabTotalPages(data.totalPages || 0);
        } catch (err) {
            console.error("Failed to fetch collaborators:", err);
            setError(err.message || "Could not load collaborators.");
        } finally {
            setLoadingCollaborators(false);
        }
    }, [projectId, keycloakAuthenticated, collabPageSize]);

    useEffect(() => {
        fetchProjectDetails();
    }, [fetchProjectDetails]);

    useEffect(() => {
        if (project) {
            fetchCollaboratorsList(collabPage);
        }
    }, [project, collabPage, fetchCollaboratorsList]);


    const handleOpenAddModal = async () => {
        if (!project || !isOwner) return;
        setLoadingConnections(true);
        setError(null);
        try {
            const allConnections = await getAllMyConnections();
            const currentCollaboratorIds = collaborators.map(c => c.id);
            const projectOwnerId = project.owner.id;

            const filteredConnections = (allConnections || []).filter(
                conn => conn.id !== projectOwnerId && !currentCollaboratorIds.includes(conn.id)
            );
            setPotentialCollaborators(filteredConnections);
            setSelectedConnectionsToAdd([]);
            setAddModalOpen(true);
        } catch (err) {
            console.error("Failed to fetch connections for adding collaborators:", err);
            setError(err.message || "Could not load your connections.");
        } finally {
            setLoadingConnections(false);
        }
    };

    const handleToggleConnectionSelection = (userId) => {
        setSelectedConnectionsToAdd(prev =>
            prev.includes(userId) ? prev.filter(id => id !== userId) : [...prev, userId]
        );
    };

    const handleAddSelectedCollaborators = async () => {
        if (selectedConnectionsToAdd.length === 0) return;
        setActionLoading(prev => ({ ...prev, add: true }));
        setError(null);
        let successCount = 0;
        try {
            for (const userId of selectedConnectionsToAdd) {
                try {
                    await addProjectCollaboratorApi(projectId, userId);
                    successCount++;
                } catch (indErr) {
                    console.error(`Failed to add collaborator ${userId}:`, indErr);
                }
            }
            if (successCount > 0) {
                fetchCollaboratorsList(1);
                setCollabPage(1);
            }
            if (successCount < selectedConnectionsToAdd.length) {
                setError("Some collaborators could not be added. Please check console for details.");
            }
        } catch (err) {
            setError("An error occurred while adding collaborators.");
        } finally {
            setActionLoading(prev => ({ ...prev, add: false }));
            setAddModalOpen(false);
        }
    };


    const openRemoveConfirmDialog = (collaborator) => {
        setUserToRemove(collaborator);
        setConfirmRemoveDialogOpen(true);
    };

    const handleRemoveCollaborator = async () => {
        if (!userToRemove) return;
        const userIdToRemove = userToRemove.id;
        setActionLoading(prev => ({ ...prev, [userIdToRemove]: true }));
        setConfirmRemoveDialogOpen(false);
        setError(null);
        try {
            await removeProjectCollaboratorApi(projectId, userIdToRemove);
            setUserToRemove(null);
            fetchCollaboratorsList(collabPage);
            fetchCollaboratorsList(1); setCollabPage(1);
        } catch (err) {
            console.error(`Failed to remove collaborator ${userIdToRemove}:`, err);
            setError(err.message || `Could not remove collaborator.`);
        } finally {
            setActionLoading(prev => ({ ...prev, [userIdToRemove]: false }));
        }
    };

    const handleCollabPageChange = (event, value) => {
        setCollabPage(value);
    };


    if (loadingProject && !project) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}><CircularProgress /></Box>;
    }
    if (error && !project) {
        return (
            <Container maxWidth="md" sx={{py:2}}>
                <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(`/projects/${projectId}`)} sx={{ mb: 2 }}>Back to Project</Button>
                <Alert severity="error">{error}</Alert>
            </Container>
        );
    }
    if (!project) {
        return <Container sx={{mt:3}}><Typography>Project not found or you do not have access.</Typography></Container>;
    }


    return (
        <Container maxWidth="md" sx={{ py: 2 }}>
            <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(`/projects/${projectId}`)} sx={{ mb: 2 }}>
                Back to Project Details
            </Button>

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h4" component="h1">
                    Manage Collaborators
                </Typography>
                {isOwner && (
                    <Button
                        variant="contained"
                        startIcon={<PersonAddIcon />}
                        onClick={handleOpenAddModal}
                        disabled={loadingConnections}
                    >
                        {loadingConnections ? <CircularProgress size={20} color="inherit"/> : "Add Collaborators"}
                    </Button>
                )}
            </Box>
            <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                Project: {project.title}
            </Typography>

            {error && !loadingCollaborators && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <Paper elevation={2} sx={{mt:2}}>
                {loadingCollaborators && collaborators.length === 0 ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}><CircularProgress /></Box>
                ) : collaborators.length === 0 ? (
                    <Typography sx={{ p: 3, textAlign: 'center' }} color="textSecondary">
                        No collaborators yet (besides the owner).
                    </Typography>
                ) : (
                    <List>
                        {collaborators.map((collab, index) => (
                            <Fragment key={collab.id}>
                                <ListItem
                                    secondaryAction={
                                        isOwner && appUser?.id !== collab.id ? (
                                            <Tooltip title="Remove Collaborator">
                                                <IconButton
                                                    edge="end"
                                                    aria-label="remove collaborator"
                                                    onClick={() => openRemoveConfirmDialog(collab)}
                                                    disabled={actionLoading[collab.id]}
                                                    color="error"
                                                >
                                                    {actionLoading[collab.id] ? <CircularProgress size={20} color="inherit"/> : <GroupRemoveIcon />}
                                                </IconButton>
                                            </Tooltip>
                                        ) : null
                                    }
                                >
                                    <ListItemAvatar>
                                        <Avatar component={RouterLink} to={`/users/${collab.id}`}>
                                            {`${collab.firstName ? collab.firstName[0] : ''}${collab.lastName ? collab.lastName[0] : ''}`.toUpperCase() || (collab.username ? collab.username[0].toUpperCase() : 'U')}
                                        </Avatar>
                                    </ListItemAvatar>
                                    <ListItemText
                                        primary={
                                            <Typography component={RouterLink} to={`/users/${collab.id}`} sx={{textDecoration:'none', color:'inherit', '&:hover':{textDecoration:'underline'}}}>
                                                {`${collab.firstName || ''} ${collab.lastName || ''}`.trim() || collab.username}
                                            </Typography>
                                        }
                                        secondary={`@${collab.username || 'N/A'}`}
                                    />
                                </ListItem>
                                {index < collaborators.length - 1 && <Divider variant="inset" component="li" />}
                            </Fragment>
                        ))}
                    </List>
                )}
            </Paper>
            {collabTotalPages > 1 && !loadingCollaborators && (
                <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                    <Pagination count={collabTotalPages} page={collabPage} onChange={handleCollabPageChange} color="primary" />
                </Box>
            )}


            {/* Add Collaborators Modal */}
            <Dialog open={addModalOpen} onClose={() => setAddModalOpen(false)} maxWidth="sm" fullWidth scroll="paper">
                <DialogTitle>Add Collaborators from Your Connections</DialogTitle>
                <DialogContent dividers>
                    {loadingConnections ? <CircularProgress sx={{display:'block', mx:'auto'}} /> :
                        potentialCollaborators.length === 0 ? <Typography>No available connections to add as collaborators.</Typography> :
                            <List dense>
                                {potentialCollaborators.map(conn => (
                                    <ListItem key={conn.id} dense button onClick={() => handleToggleConnectionSelection(conn.id)}>
                                        <Checkbox
                                            edge="start"
                                            checked={selectedConnectionsToAdd.includes(conn.id)}
                                            tabIndex={-1}
                                            disableRipple
                                        />
                                        <ListItemAvatar>
                                            <Avatar sx={{width:32, height:32}}>{`${conn.firstName ? conn.firstName[0] : ''}${conn.lastName ? conn.lastName[0] : ''}`.toUpperCase() || (conn.username ? conn.username[0].toUpperCase() : 'C')}</Avatar>
                                        </ListItemAvatar>
                                        <ListItemText primary={`${conn.firstName || ''} ${conn.lastName || ''}`.trim() || conn.username} secondary={`@${conn.username || 'N/A'}`} />
                                    </ListItem>
                                ))}
                            </List>
                    }
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setAddModalOpen(false)} disabled={actionLoading.add}>Cancel</Button>
                    <Button
                        onClick={handleAddSelectedCollaborators}
                        variant="contained"
                        disabled={selectedConnectionsToAdd.length === 0 || actionLoading.add}
                    >
                        {actionLoading.add ? <CircularProgress size={24}/> : `Add Selected (${selectedConnectionsToAdd.length})`}
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Confirm Remove Collaborator Dialog */}
            <Dialog open={confirmRemoveDialogOpen} onClose={() => setConfirmRemoveDialogOpen(false)}>
                <DialogTitle>Remove Collaborator?</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Are you sure you want to remove "{`${userToRemove?.firstName || ''} ${userToRemove?.lastName || ''}`.trim() || userToRemove?.username}" from this project?
                        They will also be unassigned from any tasks in this project.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setConfirmRemoveDialogOpen(false)}>Cancel</Button>
                    <Button onClick={handleRemoveCollaborator} color="error" autoFocus disabled={actionLoading[userToRemove?.id]}>
                        {actionLoading[userToRemove?.id] ? <CircularProgress size={20}/> : "Remove"}
                    </Button>
                </DialogActions>
            </Dialog>

        </Container>
    );
}

export default ManageCollaboratorsPage;