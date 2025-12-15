import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getOwnedProjects, getCollaboratingProjects } from '../services/api';
import { useUser } from '../contexts/UserContext';
import ProjectCard from '../components/ProjectCard';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import Paper from '@mui/material/Paper';
import Divider from "@mui/material/Divider";

const ProjectListSection = ({ title, projects, loading, error }) => {
    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 200 }}>
                <CircularProgress />
            </Box>
        );
    }
    if (error) {
        return <Alert severity="error" sx={{my: 2}}>{error}</Alert>;
    }
    if (!projects || projects.length === 0) {
        return <Typography color="textSecondary" sx={{p:2, textAlign: 'center'}}>No projects in this category yet.</Typography>;
    }

    return (
        <Box sx={{ width: '100%' }}>
            <Typography variant="h5" component="h2" gutterBottom sx={{ mt: 3, mb: 1 }}>
                {title}
            </Typography>
            <Paper
                elevation={0}
                sx={{
                    display: 'flex',
                    overflowX: 'auto',
                    py: 2,
                    px: 1,
                    backgroundColor: 'transparent',
                    '&::-webkit-scrollbar': {
                        height: '8px',
                    },
                    '&::-webkit-scrollbar-thumb': {
                        backgroundColor: 'rgba(0,0,0,.2)',
                        borderRadius: '4px',
                    }
                }}
            >
                {projects.map((project) => (
                    <Box key={project.id} sx={{ flexShrink: 0 }}>
                        <ProjectCard project={project} />
                    </Box>
                ))}
            </Paper>
        </Box>
    );
};


function ProjectsOverviewPage() {
    const { keycloakAuthenticated, appUser } = useUser();
    const navigate = useNavigate();

    const [ownedProjects, setOwnedProjects] = useState([]);
    const [collaboratingProjects, setCollaboratingProjects] = useState([]);
    const [loadingOwned, setLoadingOwned] = useState(true);
    const [loadingCollab, setLoadingCollab] = useState(true);
    const [errorOwned, setErrorOwned] = useState(null);
    const [errorCollab, setErrorCollab] = useState(null);

    const initialPageSize = 20;

    const fetchProjects = useCallback(async () => {
        if (!keycloakAuthenticated || !appUser) return;

        setLoadingOwned(true);
        setErrorOwned(null);
        try {
            const ownedData = await getOwnedProjects(0, initialPageSize);
            setOwnedProjects(ownedData.content || []);
        } catch (err) {
            console.error("Failed to fetch owned projects:", err);
            setErrorOwned(err.message || "Could not load your projects.");
        } finally {
            setLoadingOwned(false);
        }

        setLoadingCollab(true);
        setErrorCollab(null);
        try {
            const collabData = await getCollaboratingProjects(0, initialPageSize);
            setCollaboratingProjects(collabData.content || []);
        } catch (err) {
            console.error("Failed to fetch collaborating projects:", err);
            setErrorCollab(err.message || "Could not load projects you collaborate on.");
        } finally {
            setLoadingCollab(false);
        }
    }, [keycloakAuthenticated, appUser, initialPageSize]);

    useEffect(() => {
        fetchProjects();
    }, [fetchProjects]);

    return (
        <Container maxWidth="xl">
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 2, mb: 3 }}>
                <Typography variant="h4" component="h1">
                    My Projects
                </Typography>
                <Button
                    variant="contained"
                    startIcon={<AddCircleOutlineIcon />}
                    onClick={() => navigate('/projects/new')}
                >
                    Create New Project
                </Button>
            </Box>

            <ProjectListSection
                title="Owned by Me"
                projects={ownedProjects}
                loading={loadingOwned}
                error={errorOwned}
            />

            <Divider sx={{ my: 4 }} />

            <ProjectListSection
                title="Collaborating On"
                projects={collaboratingProjects}
                loading={loadingCollab}
                error={errorCollab}
            />
        </Container>
    );
}

export default ProjectsOverviewPage;