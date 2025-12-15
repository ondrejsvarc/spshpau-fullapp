import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import AccountPage from './pages/AccountPage';

import ArtistProfilePage from './pages/ArtistProfilePage';
import ProducerProfilePage from './pages/ProducerProfilePage';
import CreateArtistProfilePage from './pages/CreateArtistProfilePage';
import CreateProducerProfilePage from './pages/CreateProducerProfilePage';
import MatchesPage from './pages/MatchesPage';
import UserSearchPage from './pages/UserSearchPage';

import CssBaseline from '@mui/material/CssBaseline';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import Box from '@mui/material/Box';
import ProtectedRoute from './components/ProtectedRoute';
import ConnectionsPage from "./pages/ConnectionsPage.jsx";
import BlocksPage from "./pages/BlocksPage.jsx";
import UserProfilePage from "./pages/UserProfilePage.jsx";
import ProjectsOverviewPage from './pages/ProjectsOverviewPage';
import CreateProjectPage from './pages/CreateProjectPage';
import ProjectDetailPage from './pages/ProjectDetailPage';
import ManageCollaboratorsPage from './pages/ManageCollaboratorsPage';
import CreateTaskPage from './pages/CreateTaskPage';
import TaskEditPage from './pages/TaskEditPage';
import CreateMilestonePage from "./pages/CreateMilestonePage.jsx";
import MilestoneEditPage from "./pages/MilestoneEditPage.jsx";
import FileDetailPage from "./pages/FileDetailPage.jsx";
import BudgetPage from "./pages/BudgetPage.jsx";
import CreateBudgetPage from "./pages/CreateBudgetPage.jsx";
import CreateExpensePage from "./pages/CreateExpensePage.jsx";
import ChatPage from "./pages/ChatPage.jsx";

const theme = createTheme({
    palette: {
        primary: { main: '#1976d2' },
        secondary: { main: '#dc004e' },
    },
});

function App() {
    return (
        <Router>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', width: '100%' }}>
                    <Navbar />
                    <Box
                        component="main"
                        sx={{
                            flexGrow: 1,
                            py: 3,
                            px: { xs: 2, sm: 3 },
                            width: '100%',
                            display: 'flex',
                            flexDirection: 'column',
                        }}
                    >
                        <Routes>
                            <Route path="/" element={<HomePage />} />
                            <Route path="/account" element={
                                <ProtectedRoute>
                                    <AccountPage />
                                </ProtectedRoute>
                            } />
                            <Route path="/account/artist-profile" element={
                                <ProtectedRoute><ArtistProfilePage /></ProtectedRoute>
                            } />
                            <Route path="/account/producer-profile" element={
                                <ProtectedRoute><ProducerProfilePage /></ProtectedRoute>
                            } />
                            <Route path="/account/artist-profile/create" element={
                                <ProtectedRoute><CreateArtistProfilePage /></ProtectedRoute>
                            } />
                            <Route path="/account/producer-profile/create" element={
                                <ProtectedRoute><CreateProducerProfilePage /></ProtectedRoute>
                            } />
                            <Route path="/connections" element={
                                <ProtectedRoute><ConnectionsPage /></ProtectedRoute>
                            } />
                            <Route path="/blocks" element={
                                <ProtectedRoute><BlocksPage /></ProtectedRoute>
                            } />
                            <Route path="/users/:userId" element={
                                <ProtectedRoute><UserProfilePage /></ProtectedRoute>
                            } />
                            <Route path="/users/search" element={
                                <ProtectedRoute><UserSearchPage /></ProtectedRoute>
                            } />
                            <Route path="/users/matches" element={
                                <ProtectedRoute><MatchesPage /></ProtectedRoute>
                            } />
                            <Route path="/projects" element={
                                <ProtectedRoute><ProjectsOverviewPage /></ProtectedRoute>
                            } />
                            <Route path="/projects/new" element={
                                <ProtectedRoute><CreateProjectPage /></ProtectedRoute>
                            } />
                            <Route path="/projects/:projectId" element={
                                <ProtectedRoute><ProjectDetailPage /></ProtectedRoute>
                            } />
                            <Route path="/projects/:projectId" element={
                                <ProtectedRoute><ProjectDetailPage /></ProtectedRoute>
                            } />
                            <Route path="/projects/:projectId/collaborators" element={
                                <ProtectedRoute><ManageCollaboratorsPage /></ProtectedRoute>
                            } />
                            <Route path="/projects/:projectId/tasks/new" element={
                                <ProtectedRoute><CreateTaskPage /></ProtectedRoute>
                            } />
                            <Route path="/projects/:projectId/tasks/:taskId/edit" element={
                                <ProtectedRoute><TaskEditPage /></ProtectedRoute>
                            } />
                            <Route path="/projects/:projectId/milestones/new" element={
                                <ProtectedRoute><CreateMilestonePage /></ProtectedRoute>
                            } />
                            <Route path="/projects/:projectId/milestones/:milestoneId/edit" element={
                                <ProtectedRoute><MilestoneEditPage /></ProtectedRoute>
                            } />
                            <Route path="/projects/:projectId/budget/create" element={
                                <ProtectedRoute><CreateBudgetPage /></ProtectedRoute>
                            } />
                            <Route path="/projects/:projectId/file-versions" element={
                                <ProtectedRoute><FileDetailPage /></ProtectedRoute>
                            } />
                            <Route path="/projects/:projectId/budget" element={
                                <ProtectedRoute><BudgetPage /></ProtectedRoute>
                            } />
                            <Route path="/projects/:projectId/budget/expenses/new" element={
                                <ProtectedRoute><CreateExpensePage /></ProtectedRoute>
                            } />
                            <Route path="/chat" element={
                                <ProtectedRoute><ChatPage /></ProtectedRoute>
                            } />
                            <Route path="/chat/:recipientId" element={
                                <ProtectedRoute><ChatPage /></ProtectedRoute>
                            } />
                        </Routes>
                    </Box>
                </Box>
            </ThemeProvider>
        </Router>
    );
}

export default App;