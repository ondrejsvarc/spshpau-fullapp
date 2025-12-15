import React from 'react';
import { useKeycloak } from '@react-keycloak/web';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import {alpha, styled} from '@mui/material/styles';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import FolderSpecialIcon from '@mui/icons-material/FolderSpecial';
import ChatIcon from '@mui/icons-material/Chat';
import { useNavigate } from 'react-router-dom';

const TilePaper = styled(Paper)(({ theme }) => ({
    ...theme.typography.body2,
    padding: theme.spacing(2),
    textAlign: 'center',
    color: theme.palette.text.secondary,
    height: 150,
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    cursor: 'pointer',
    '&:hover': {
        backgroundColor: alpha(theme.palette.action.hover, 0.1),
        boxShadow: theme.shadows[4],
    },
}));

function HomePage() {
    const { keycloak, initialized } = useKeycloak();
    const navigate = useNavigate();

    if (!initialized) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 5 }}><Typography>Loading authentication...</Typography></Box>;
    }

    return (
        <Container maxWidth="lg"> {}
            <Box sx={{ my: 4 }}>
                {!keycloak.authenticated ? (
                    // NOT LOGGED IN VIEW
                    <Box textAlign="center">
                        <Typography variant="h3" component="h1" gutterBottom>
                            Welcome to SPSHPAU
                        </Typography>
                        <Typography variant="h5" component="h2" color="text.secondary" paragraph>
                            Connect, Collaborate, and Create Music.
                        </Typography>
                        <Typography variant="body1" paragraph>
                            Please log in to access your dashboard and services.
                        </Typography>
                        <Button
                            variant="contained"
                            color="primary"
                            size="large"
                            onClick={() => keycloak.login()}
                            sx={{ mt: 2 }}
                        >
                            Login with Keycloak
                        </Button>
                    </Box>
                ) : (
                    // LOGGED IN VIEW
                    <Box>
                        <Typography variant="h4" component="h1" gutterBottom align="center" sx={{ mb: 4 }}>
                            Your Dashboard
                        </Typography>
                        <Grid container spacing={4} justifyContent="center">
                            <Grid item xs={12} sm={6} md={4}>
                                <TilePaper onClick={() => navigate('/users/matches')}>
                                    <AccountCircleIcon sx={{ fontSize: 40, mb: 1 }} color="primary"/>
                                    <Typography variant="h6">Look for Collaborators</Typography>
                                    <Typography variant="body2">Find collaborators and search users.</Typography>
                                </TilePaper>
                            </Grid>
                            <Grid item xs={12} sm={6} md={4}>
                                <TilePaper onClick={() => navigate('/projects')}>
                                    <FolderSpecialIcon sx={{ fontSize: 40, mb: 1 }} color="secondary"/>
                                    <Typography variant="h6">Project Service</Typography>
                                    <Typography variant="body2">Organize your musical projects.</Typography>
                                </TilePaper>
                            </Grid>
                            <Grid item xs={12} sm={6} md={4}>
                                <TilePaper onClick={() => navigate('/chat')}>
                                    <ChatIcon sx={{ fontSize: 40, mb: 1 }} style={{ color: '#4caf50' }}/>
                                    <Typography variant="h6">Chat Service</Typography>
                                    <Typography variant="body2">Communicate with collaborators.</Typography>
                                </TilePaper>
                            </Grid>
                        </Grid>
                    </Box>
                )}
            </Box>
        </Container>
    );
}

export default HomePage;