import React, {useState, useRef, useEffect} from 'react';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import { Typography } from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';

const ALLOWED_MIME_TYPES = ['audio/mpeg', 'audio/wav', 'audio/x-wav', 'application/pdf', 'audio/mp3'];
const MAX_FILE_SIZE_MB = 50;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;

function FileUploadModal({ open, onClose, projectId, onFileUploadSuccess, existingFilename = null }) {
    const [selectedFile, setSelectedFile] = useState(null);
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [fileNameDisplay, setFileNameDisplay] = useState('');
    const fileInputRef = useRef(null);

    useEffect(() => {
        if (open) {
            setSelectedFile(null);
            setDescription('');
            setError(null);
            setLoading(false);
            setFileNameDisplay(existingFilename || '');
            if (fileInputRef.current) {
                fileInputRef.current.value = "";
            }
        }
    }, [open, existingFilename]);

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (file) {
            if (!ALLOWED_MIME_TYPES.includes(file.type)) {
                setError(`Invalid file type. Allowed: MP3, WAV, PDF. Got: ${file.type}`);
                setSelectedFile(null);
                setFileNameDisplay('');
                return;
            }
            if (file.size > MAX_FILE_SIZE_BYTES) {
                setError(`File is too large. Max size: ${MAX_FILE_SIZE_MB}MB. Got: ${(file.size / (1024*1024)).toFixed(2)}MB`);
                setSelectedFile(null);
                setFileNameDisplay('');
                return;
            }
            setSelectedFile(file);
            setFileNameDisplay(file.name);
            setError(null);
        }
    };

    const handleSubmit = async () => {
        if (!selectedFile && !existingFilename) {
            setError('Please select a file to upload.');
            return;
        }
        if (!selectedFile && existingFilename) {
            setError('Please select a file for the new version.');
            return;
        }


        setLoading(true);
        setError(null);

        const formData = new FormData();
        formData.append('file', selectedFile);
        formData.append('description', description);

        console.log("FormData about to be submitted:");
        for (let [key, value] of formData.entries()) {
            console.log(key, value);
        }

        try {
            await onFileUploadSuccess(formData, existingFilename);
            handleCloseDialog();
        } catch (err) {
            console.error("File upload failed:", err);
            setError(err.message || "File upload failed. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    const handleCloseDialog = () => {
        if (loading) return;
        onClose();
    };

    return (
        <Dialog open={open} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
            <DialogTitle>
                {existingFilename ? `Upload New Version for "${existingFilename}"` : 'Upload New Project File'}
            </DialogTitle>
            <DialogContent>
                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                <Box component="form" noValidate sx={{ mt: 1 }}>
                    <Button
                        variant="outlined"
                        component="label"
                        fullWidth
                        startIcon={<CloudUploadIcon />}
                        sx={{ mb: 2 }}
                        disabled={loading}
                    >
                        {selectedFile ? `Selected: ${fileNameDisplay}` : "Choose File"}
                        <input
                            type="file"
                            hidden
                            onChange={handleFileChange}
                            ref={fileInputRef}
                            accept={ALLOWED_MIME_TYPES.join(',')}
                        />
                    </Button>
                    {fileNameDisplay && !existingFilename && selectedFile && fileNameDisplay !== selectedFile.name && (
                        <Typography variant="caption" color="textSecondary" display="block" sx={{mb:1}}>
                            Original name: {selectedFile.name}
                        </Typography>
                    )}
                    {existingFilename && selectedFile && (
                        <Typography variant="caption" color="textSecondary" display="block" sx={{mb:1}}>
                            Uploading as new version of: {existingFilename}. Selected file: {selectedFile.name}
                        </Typography>
                    )}


                    <TextField
                        margin="dense"
                        id="description"
                        label="File Description (Optional)"
                        type="text"
                        fullWidth
                        multiline
                        rows={3}
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        disabled={loading}
                    />
                </Box>
            </DialogContent>
            <DialogActions sx={{p: '16px 24px'}}>
                <Button onClick={handleCloseDialog} disabled={loading}>Cancel</Button>
                <Button onClick={handleSubmit} variant="contained" disabled={loading || !selectedFile}>
                    {loading ? <CircularProgress size={24} /> : 'Upload'}
                </Button>
            </DialogActions>
        </Dialog>
    );
}

export default FileUploadModal;