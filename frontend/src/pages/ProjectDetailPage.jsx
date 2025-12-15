import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link as RouterLink } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import {
    getProjectById,
    deleteProjectApi,
    getProjectTasks,
    getProjectMilestones,
    getProjectFiles,
    getProjectFileDownloadUrl,
    getProjectCollaborators,
    getProjectBudget,
    getRemainingProjectBudget,
    createProjectBudgetApi
} from '../services/api';

// MUI Components
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Grid from '@mui/material/Grid';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Divider from '@mui/material/Divider';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardActions from '@mui/material/CardActions';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';
import Avatar from '@mui/material/Avatar';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import ListItemText from '@mui/material/ListItemText';
import Tooltip from '@mui/material/Tooltip';

// Icons
import DeleteIcon from '@mui/icons-material/Delete';
import GroupIcon from '@mui/icons-material/Group';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import FolderIcon from '@mui/icons-material/Folder';
import TaskIcon from '@mui/icons-material/AssignmentTurnedIn';
import MilestoneIcon from '@mui/icons-material/Flag';
import FileIcon from '@mui/icons-material/AttachFile';
import DownloadIcon from '@mui/icons-material/Download';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import VersionsIcon from '@mui/icons-material/History';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';

import FileUploadModal from '../components/FileUploadModal';
import { uploadProjectFileApi } from '../services/api';


// --- Horizontal Scroll Section ---
export const HorizontalScrollSection = ({ title, children, actionButton }) => (
    <Box sx={{ width: '100%', mt: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
            <Typography variant="h5" component="h2" gutterBottom>
                {title}
            </Typography>
            {actionButton}
        </Box>
        <Paper
            elevation={0}
            sx={{
                display: 'flex',
                overflowX: 'auto',
                py: 2,
                px: 1,
                backgroundColor: 'transparent',
                '&::-webkit-scrollbar': { height: '8px' },
                '&::-webkit-scrollbar-thumb': { backgroundColor: 'rgba(0,0,0,.2)', borderRadius: '4px' }
            }}
        >
            {children}
        </Paper>
        <Divider sx={{my:2}}/>
    </Box>
);

// --- Task Card ---
const TaskCard = ({ task, projectId }) => {
    const navigate = useNavigate();
    const assignedTo = task.assignedUser
        ? `${task.assignedUser.firstName || ''} ${task.assignedUser.lastName || ''}`.trim() || task.assignedUser.username
        : 'Unassigned';

    return (
        <Card sx={{ minWidth: 280, maxWidth: 300, m: 1, display: 'flex', flexDirection: 'column', height: '100%' }}>
            <CardContent sx={{ flexGrow: 1 }}>
                <Typography variant="h6" component="div" noWrap title={task.title}>{task.title}</Typography>
                <Typography sx={{ mb: 1.5 }} color="text.secondary">Status: {task.status}</Typography>
                <Typography variant="body2" sx={{ mb: 1, height: 60, overflow: "hidden", textOverflow: "ellipsis", display: "-webkit-box", WebkitLineClamp: 3, WebkitBoxOrient: "vertical" }}>
                    {task.description || "No description."}
                </Typography>
                {task.dueDate && <Typography variant="caption" display="block">Due: {new Date(task.dueDate).toLocaleDateString()}</Typography>}
                <Typography variant="caption" display="block">
                    Assigned to: {task.assignedUser ? (
                    <RouterLink to={`/users/${task.assignedUser.id}`} style={{textDecoration:'none'}}>
                        {assignedTo}
                    </RouterLink>
                ) : assignedTo}
                </Typography>
            </CardContent>
            <CardActions>
                <Button size="small" onClick={() => navigate(`/projects/${projectId}/tasks/${task.id}/edit`)}>Edit/Delete</Button>
            </CardActions>
        </Card>
    );
};

// --- Milestone Card ---
const MilestoneCard = ({ milestone, projectId }) => {
    const navigate = useNavigate();
    return (
        <Card sx={{ minWidth: 280, maxWidth: 300, m: 1, display: 'flex', flexDirection: 'column', height: '100%' }}>
            <CardContent sx={{ flexGrow: 1 }}>
                <Typography variant="h6" component="div" noWrap title={milestone.title}>{milestone.title}</Typography>
                {milestone.dueDate && <Typography sx={{ mb: 1.5 }} color="text.secondary">Due: {new Date(milestone.dueDate).toLocaleDateString()}</Typography>}
                <Typography variant="body2" sx={{ mb: 1, height: 80, overflow: "hidden", textOverflow: "ellipsis", display: "-webkit-box", WebkitLineClamp: 4, WebkitBoxOrient: "vertical" }}>
                    {milestone.description || "No description."}
                </Typography>
            </CardContent>
            <CardActions>
                <Button size="small" onClick={() => navigate(`/projects/${projectId}/milestones/${milestone.id}/edit`)}>Edit/Delete</Button>
            </CardActions>
        </Card>
    );
};

// --- File Card ---
const FileCard = ({ file, projectId, onUploadNewVersionClick }) => {
    const navigate = useNavigate();
    const handleDownload = async () => {
        try {
            const { downloadUrl } = await getProjectFileDownloadUrl(projectId, file.id);
            window.open(downloadUrl, '_blank');
        } catch (error) {
            console.error("Failed to get download URL", error);
            alert("Error: Could not get download link.");
        }
    };
    return (
        <Card sx={{ minWidth: 300, maxWidth: 320, m: 1, display: 'flex', flexDirection: 'column', height: '100%' }}>
            <CardContent sx={{ flexGrow: 1 }}>
                <Box display="flex" alignItems="center" mb={1}>
                    <FileIcon sx={{mr:1}} color="action"/>
                    <Typography variant="h6" component="div" noWrap title={file.originalFilename}>{file.originalFilename}</Typography>
                </Box>
                <Typography variant="caption" display="block">Type: {file.contentType}</Typography>
                <Typography variant="caption" display="block">Size: {(file.fileSize / 1024).toFixed(2)} KB</Typography>
                <Typography variant="caption" display="block">Uploaded: {new Date(file.uploadTimestamp).toLocaleDateString()}</Typography>
                {file.uploadedBy && <Typography variant="caption" display="block">By: {file.uploadedBy.username}</Typography>}
                <Typography variant="body2" sx={{ mt: 1, height: 40, overflow: "hidden", textOverflow: "ellipsis", display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical" }}>
                    {file.description || "No description."}
                </Typography>
            </CardContent>
            <CardActions sx={{justifyContent:'space-around', flexWrap:'wrap'}}>
                <Button size="small" startIcon={<UploadFileIcon/>} onClick={() => onUploadNewVersionClick(file.originalFilename)}>
                    New Version
                </Button>
                <Button size="small" startIcon={<VersionsIcon/>} onClick={() => navigate(`/projects/${projectId}/file-versions?filename=${encodeURIComponent(file.originalFilename)}`)}>Versions</Button>
                <Button size="small" startIcon={<DownloadIcon/>} onClick={handleDownload}>Download</Button>
            </CardActions>
        </Card>
    );
};


function ProjectDetailPage() {
    const { projectId } = useParams();
    const navigate = useNavigate();
    const { appUser, keycloakAuthenticated } = useUser();

    const [project, setProject] = useState(null);
    const [tasks, setTasks] = useState([]);
    const [milestones, setMilestones] = useState([]);
    const [files, setFiles] = useState([]);
    const [collaborators, setCollaborators] = useState([]);
    const [budgetSummary, setBudgetSummary] = useState(null);
    const [projectHasBudget, setProjectHasBudget] = useState(false);
    const [fileUploadModalOpen, setFileUploadModalOpen] = useState(false);
    const [uploadingForFilename, setUploadingForFilename] = useState(null);

    const [loading, setLoading] = useState({
        project: true, tasks: true, milestones: true, files: true, collaborators: true, budget: true
    });
    const [error, setError] = useState(null);
    const [confirmDeleteDialogOpen, setConfirmDeleteDialogOpen] = useState(false);

    const isOwner = appUser?.id === project?.owner?.id;

    const fetchData = useCallback(async () => {
        if (!keycloakAuthenticated || !projectId) return;
        setLoading(prev => ({ ...prev, project: true, tasks: true, milestones: true, files: true, collaborators: true, budget: true }));
        setError(null);
        try {
            const projectData = await getProjectById(projectId);
            setProject(projectData);
            setLoading(prev => ({ ...prev, project: false }));

            // Fetch related data in parallel
            Promise.allSettled([
                getProjectTasks(projectId, 0, 20).then(data => { setTasks(data.content || []); setLoading(prev => ({ ...prev, tasks: false })); }),
                getProjectMilestones(projectId, 0, 20).then(data => { setMilestones(data.content || []); setLoading(prev => ({ ...prev, milestones: false })); }),
                getProjectFiles(projectId).then(data => { setFiles(data || []); setLoading(prev => ({ ...prev, files: false })); }), // getProjectFiles directly returns array
                getProjectCollaborators(projectId, 0, 20).then(data => { setCollaborators(data.content || []); setLoading(prev => ({ ...prev, collaborators: false })); }),
                getProjectBudget(projectId)
                    .then(budget => {
                        setProjectHasBudget(true);
                        // If budget exists, fetch remaining summary
                        return getRemainingProjectBudget(projectId);
                    })
                    .then(summary => {
                        setBudgetSummary(summary);
                    })
                    .catch(budgetError => {
                        if (budgetError.message && budgetError.message.includes("404")) {
                            setProjectHasBudget(false);
                            setBudgetSummary(null);
                        } else {
                            console.error("Error fetching budget:", budgetError);
                            setError(prev => ({...prev, budget: budgetError.message}));
                        }
                    }).finally(() => setLoading(prev => ({ ...prev, budget: false })))
            ]).catch(overallError => {
                console.error("Error fetching some project sub-resources:", overallError);
            });

        } catch (err) {
            console.error("Failed to fetch project details:", err);
            setError(err.message || "Could not load project details.");
            setLoading(prev => ({ ...prev, project: false, tasks: false, milestones: false, files: false, collaborators: false, budget: false }));
        }
    }, [projectId, keycloakAuthenticated]);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const handleDeleteProject = async () => {
        setConfirmDeleteDialogOpen(false);
        setLoading(prev => ({ ...prev, project: true }));
        try {
            await deleteProjectApi(projectId);
            alert('Project deleted successfully!');
            navigate('/projects');
        } catch (err) {
            console.error("Failed to delete project:", err);
            setError(err.message || "Could not delete project.");
            setLoading(prev => ({ ...prev, project: false }));
        }
    };

    const handleCreateBudget = async () => {
        navigate(`/projects/${projectId}/budget/create`);
    }

    const handleOpenFileUploadModal = (filename = null) => {
        setUploadingForFilename(filename);
        setFileUploadModalOpen(true);
    };

    const handleFileUploadSuccess = async (formData) => {

        try {
            await uploadProjectFileApi(projectId, formData);
            setLoading(prev => ({ ...prev, files: true }));
            getProjectFiles(projectId)
                .then(data => setFiles(data || []))
                .catch(err => {
                    console.error("Failed to refresh files after upload:", err);
                    setError("File uploaded, but failed to refresh file list.");
                })
                .finally(() => setLoading(prev => ({ ...prev, files: false })));
            return Promise.resolve();
        } catch (uploadError) {
            console.error("Actual upload failed:", uploadError);
            return Promise.reject(uploadError);
        }
    };

    if (loading.project && !project) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 5 }}><CircularProgress /></Box>;
    if (error && !project) return <Container sx={{mt:3}}><Alert severity="error">{error}</Alert></Container>;
    if (!project) return <Container sx={{mt:3}}><Typography>Project not found.</Typography></Container>;

    return (
        <Container maxWidth="xl" sx={{ py: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Box>
                    <Typography variant="h3" component="h1">{project.title}</Typography>
                    <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                        Owned by: {project.owner?.firstName || ''} {project.owner?.lastName || ''} ({project.owner?.username})
                    </Typography>
                </Box>
                {isOwner && (
                    <Box>
                        <Button
                            variant="outlined"
                            startIcon={<GroupIcon />}
                            sx={{ mr: 1 }}
                            onClick={() => navigate(`/projects/${projectId}/collaborators`)}
                        >
                            Manage Collaborators
                        </Button>
                        <Button
                            variant="contained"
                            color="error"
                            startIcon={<DeleteIcon />}
                            onClick={() => setConfirmDeleteDialogOpen(true)}
                        >
                            Delete Project
                        </Button>
                    </Box>
                )}
            </Box>
            <Typography variant="body1" paragraph sx={{mb:3}}>{project.description || "No description provided."}</Typography>

            <Divider sx={{my:3}}/>

            {/* Tasks Section */}
            <HorizontalScrollSection
                title="Tasks"
                actionButton={
                    <Button variant="contained" size="small" startIcon={<AddCircleOutlineIcon />} onClick={() => navigate(`/projects/${projectId}/tasks/new`)}>
                        New Task
                    </Button>
                }
            >
                {loading.tasks ? <CircularProgress sx={{mx: 'auto'}} /> :
                    tasks.length > 0 ? tasks.map(task => <Box key={task.id} sx={{flexShrink: 0}}><TaskCard task={task} projectId={projectId} /></Box>) :
                        <Typography sx={{pl:1, color:'text.secondary'}}>No tasks yet.</Typography>}
            </HorizontalScrollSection>

            {/* Milestones Section */}
            <HorizontalScrollSection
                title="Milestones"
                actionButton={
                    <Button variant="contained" size="small" startIcon={<AddCircleOutlineIcon />} onClick={() => navigate(`/projects/${projectId}/milestones/new`)}>
                        New Milestone
                    </Button>
                }
            >
                {loading.milestones ? <CircularProgress sx={{mx: 'auto'}}/> :
                    milestones.length > 0 ? milestones.map(ms => <Box key={ms.id} sx={{flexShrink: 0}}><MilestoneCard milestone={ms} projectId={projectId} /></Box>) :
                        <Typography sx={{pl:1, color:'text.secondary'}}>No milestones defined.</Typography>}
            </HorizontalScrollSection>

            {/* Files Section */}
            <HorizontalScrollSection
                title="Files"
                actionButton={
                    <Button
                        variant="contained"
                        size="small"
                        startIcon={<UploadFileIcon />}
                        onClick={() => handleOpenFileUploadModal()}
                    >
                        Upload New File
                    </Button>
                }
            >
                {loading.files ? <CircularProgress sx={{mx: 'auto'}} /> :
                    files.length > 0 ? files.map(file => (
                            <Box key={file.id} sx={{flexShrink: 0}}>
                                <FileCard
                                    file={file}
                                    projectId={projectId}
                                    onUploadNewVersionClick={() => handleOpenFileUploadModal(file.originalFilename)}
                                />
                            </Box>
                        )) :
                        <Typography sx={{pl:1, color:'text.secondary'}}>No files uploaded.</Typography>}
            </HorizontalScrollSection>


            {/* Collaborators & Budget */}
            <Grid container spacing={3} sx={{ mt: 2 }}>
                <Grid item xs={12} md={6}>
                    <Paper elevation={2} sx={{ p: 2, height: '100%' }}>
                        <Typography variant="h5" gutterBottom>Collaborators</Typography>
                        {loading.collaborators ? <CircularProgress /> :
                            collaborators.length > 0 ? (
                                <List dense>
                                    {collaborators.map(collab => (
                                        <ListItem key={collab.id} button component={RouterLink} to={`/users/${collab.id}`}>
                                            <ListItemAvatar>
                                                <Avatar>{`${collab.firstName ? collab.firstName[0] : ''}${collab.lastName ? collab.lastName[0] : ''}`.toUpperCase() || (collab.username ? collab.username[0].toUpperCase() : 'U')}</Avatar>
                                            </ListItemAvatar>
                                            <ListItemText primary={`${collab.firstName || ''} ${collab.lastName || ''}`.trim() || collab.username} secondary={`@${collab.username || 'N/A'}`} />
                                        </ListItem>
                                    ))}
                                </List>
                            ) : <Typography color="textSecondary">No collaborators yet (besides the owner).</Typography>}
                    </Paper>
                </Grid>

                <Grid item xs={12} md={6}>
                    <Paper elevation={2} sx={{ p: 2, height: '100%' }}>
                        <Typography variant="h5" gutterBottom>Budget</Typography>
                        {loading.budget ? <CircularProgress /> :
                            projectHasBudget && budgetSummary ? (
                                <Box>
                                    <Typography variant="h6">{budgetSummary.totalAmount?.toFixed(2)} {budgetSummary.currency}</Typography>
                                    <Typography color="textSecondary">Total Budgeted</Typography>
                                    <Typography variant="body1" sx={{color: budgetSummary.remainingAmount < 0 ? 'error.main' : 'success.main', mt:1}}>
                                        Remaining: {budgetSummary.remainingAmount?.toFixed(2)} {budgetSummary.currency}
                                    </Typography>
                                    <Typography variant="caption" display="block">Spent: {budgetSummary.spentAmount?.toFixed(2)} {budgetSummary.currency}</Typography>
                                    <Button variant="outlined" sx={{mt:2}} onClick={() => navigate(`/projects/${projectId}/budget`)}>View/Manage Budget</Button>
                                </Box>
                            ) : isOwner ? (
                                <Box sx={{textAlign:'center', pt:2}}>
                                    <Typography color="textSecondary" sx={{mb:1}}>No budget set for this project.</Typography>
                                    <Button variant="contained" onClick={handleCreateBudget}>Create Budget</Button>
                                </Box>
                            ) : <Typography color="textSecondary">No budget information available.</Typography>}
                    </Paper>
                </Grid>
            </Grid>

            {/* File Upload Modal */}
            <FileUploadModal
                open={fileUploadModalOpen}
                onClose={() => setFileUploadModalOpen(false)}
                projectId={projectId}
                onFileUploadSuccess={handleFileUploadSuccess}
                existingFilename={uploadingForFilename}
            />

            {/* Delete Project Confirmation Dialog */}
            <Dialog
                open={confirmDeleteDialogOpen}
                onClose={() => setConfirmDeleteDialogOpen(false)}
            >
                <DialogTitle>Delete Project "{project?.title}"?</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Are you sure you want to delete this project? This action cannot be undone, and all associated tasks, milestones, files, and budget information will be permanently removed.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setConfirmDeleteDialogOpen(false)}>Cancel</Button>
                    <Button onClick={handleDeleteProject} color="error" autoFocus>
                        Delete Project
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
}

export default ProjectDetailPage;