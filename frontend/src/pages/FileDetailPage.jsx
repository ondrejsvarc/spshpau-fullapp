import React, {useState, useEffect, useCallback, Fragment} from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import { getAllVersionsOfFile, getProjectFileDownloadUrl, deleteProjectFileApi } from '../services/api';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import IconButton from '@mui/material/IconButton';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Divider from '@mui/material/Divider';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';
import Tooltip from '@mui/material/Tooltip';

// Icons
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import DownloadIcon from '@mui/icons-material/Download';
import DeleteIcon from '@mui/icons-material/Delete';
import FilePresentIcon from '@mui/icons-material/FilePresent';
import ListItemAvatar from "@mui/material/ListItemAvatar";
import Avatar from "@mui/material/Avatar";


function FileDetailPage() {
    const { projectId } = useParams();
    const [searchParams] = useSearchParams();
    const originalFilename = searchParams.get('filename');
    const navigate = useNavigate();
    const { appUser, keycloakAuthenticated } = useUser();

    const [fileVersions, setFileVersions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [actionLoading, setActionLoading] = useState({});

    const [confirmDeleteDialogOpen, setConfirmDeleteDialogOpen] = useState(false);
    const [fileToDelete, setFileToDelete] = useState(null);


    const fetchVersions = useCallback(async () => {
        if (!keycloakAuthenticated || !projectId || !originalFilename) {
            setError("Project ID or filename missing.");
            setLoading(false);
            return;
        }
        setLoading(true);
        setError(null);
        try {
            const data = await getAllVersionsOfFile(projectId, originalFilename);
            setFileVersions(data || []);
        } catch (err) {
            console.error("Failed to fetch file versions:", err);
            setError(err.message || "Could not load file versions.");
        } finally {
            setLoading(false);
        }
    }, [projectId, originalFilename, keycloakAuthenticated]);

    useEffect(() => {
        fetchVersions();
    }, [fetchVersions]);

    const handleDownload = async (fileId, filenameToDownload) => {
        setActionLoading(prev => ({ ...prev, [fileId]: true }));
        try {
            const { downloadUrl } = await getProjectFileDownloadUrl(projectId, fileId);
            const link = document.createElement('a');
            link.href = downloadUrl;
            link.setAttribute('download', filenameToDownload || 'download');
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        } catch (err) {
            console.error("Failed to get download URL for version:", err);
            alert(`Error downloading file: ${err.message}`);
        } finally {
            setActionLoading(prev => ({ ...prev, [fileId]: false }));
        }
    };

    const openDeleteConfirm = (fileId, filename) => {
        setFileToDelete({ fileId, filename });
        setConfirmDeleteDialogOpen(true);
    };

    const handleDeleteFile = async () => {
        if (!fileToDelete) return;
        const { fileId } = fileToDelete;
        setActionLoading(prev => ({ ...prev, [fileId]: true }));
        setConfirmDeleteDialogOpen(false);
        try {
            await deleteProjectFileApi(projectId, fileId);
            setFileToDelete(null);
            fetchVersions();
        } catch (err) {
            console.error(`Failed to delete file version ${fileId}:`, err);
            setError(`Failed to delete file version: ${err.message}`);
        } finally {
            setActionLoading(prev => ({ ...prev, [fileId]: false }));
        }
    };

    if (loading) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', alignItems:'center', height:'80vh' }}><CircularProgress /></Box>;
    }

    return (
        <Container maxWidth="lg" sx={{ py: 3 }}>
            <Button
                startIcon={<ArrowBackIcon />}
                onClick={() => navigate(`/projects/${projectId}`)}
                sx={{ mb: 2 }}
            >
                Back to Project
            </Button>

            <Typography variant="h4" component="h1" gutterBottom>
                Versions of: {originalFilename || "File"}
            </Typography>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            {fileVersions.length === 0 && !loading && (
                <Typography color="textSecondary" sx={{mt:3, textAlign:'center'}}>
                    No versions found for this file or file does not exist.
                </Typography>
            )}

            {fileVersions.length > 0 && (
                <Paper elevation={2}>
                    <List>
                        {fileVersions.map((version, index) => (
                            <Fragment key={version.id}>
                                <ListItem
                                    secondaryAction={
                                        <Box>
                                            <Tooltip title="Download this version">
                                                <IconButton
                                                    edge="end"
                                                    aria-label="download version"
                                                    onClick={() => handleDownload(version.id, version.originalFilename)}
                                                    disabled={actionLoading[version.id]}
                                                >
                                                    {actionLoading[version.id] && actionLoading[version.id] === 'download' ? <CircularProgress size={24}/> : <DownloadIcon />}
                                                </IconButton>
                                            </Tooltip>
                                            <Tooltip title="Delete this version">
                                                <IconButton
                                                    edge="end"
                                                    aria-label="delete version"
                                                    onClick={() => openDeleteConfirm(version.id, version.originalFilename)}
                                                    disabled={actionLoading[version.id]}
                                                    sx={{ ml: 1 }}
                                                    color="error"
                                                >
                                                    {actionLoading[version.id] && actionLoading[version.id] === 'delete' ? <CircularProgress size={24} color="error"/> : <DeleteIcon />}
                                                </IconButton>
                                            </Tooltip>
                                        </Box>
                                    }
                                >
                                    <ListItemAvatar>
                                        <Avatar><FilePresentIcon /></Avatar>
                                    </ListItemAvatar>
                                    <ListItemText
                                        primary={`Version uploaded: ${new Date(version.uploadTimestamp).toLocaleString()}`}
                                        secondary={
                                            <>
                                                <Typography component="span" variant="body2" color="textPrimary">
                                                    ID: {version.id}
                                                </Typography>
                                                <br />
                                                <Typography component="span" variant="caption" color="textSecondary">
                                                    Uploader: @{version.uploadedBy?.username || 'N/A'}
                                                </Typography>
                                                <br />
                                                <Typography component="span" variant="caption" color="textSecondary">
                                                    Size: {(version.fileSize / 1024).toFixed(1)} KB | Type: {version.contentType}
                                                </Typography>
                                                {version.description && (
                                                    <>
                                                        <br />
                                                        <Typography component="span" variant="caption" color="textSecondary">
                                                            Description: {version.description}
                                                        </Typography>
                                                    </>
                                                )}
                                            </>
                                        }
                                    />
                                </ListItem>
                                {index < fileVersions.length - 1 && <Divider component="li" />}
                            </Fragment>
                        ))}
                    </List>
                </Paper>
            )}

            {/* Delete Confirmation Dialog */}
            <Dialog
                open={confirmDeleteDialogOpen}
                onClose={() => setConfirmDeleteDialogOpen(false)}
            >
                <DialogTitle>Delete File Version?</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Are you sure you want to delete this version of the file: "{fileToDelete?.filename}" (Version ID: {fileToDelete?.fileId})? This action cannot be undone.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setConfirmDeleteDialogOpen(false)}>Cancel</Button>
                    <Button onClick={handleDeleteFile} color="error" autoFocus>
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
}

export default FileDetailPage;