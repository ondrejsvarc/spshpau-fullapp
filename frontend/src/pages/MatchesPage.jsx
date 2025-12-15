import React, { useState, useEffect, useCallback } from 'react';
import { getMatches } from '../services/api';
import { useUser } from '../contexts/UserContext';
import UserListItem from '../components/UserListItem';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import List from '@mui/material/List';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Pagination from '@mui/material/Pagination';
import Paper from '@mui/material/Paper';
import Divider from '@mui/material/Divider';

function MatchesPage() {
    console.log('MatchesPage: Component rendering/mounting.');
    const { keycloakAuthenticated } = useUser();
    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);

    console.log('MatchesPage: Initial state - keycloakAuthenticated:', keycloakAuthenticated);

    const fetchMatches = useCallback(async (currentPage) => {
        console.log('MatchesPage: fetchMatches called. keycloakAuthenticated:', keycloakAuthenticated);
        if (!keycloakAuthenticated) {
            console.log('MatchesPage: Not authenticated, skipping fetchMatches.');
            setLoading(false);
            return;
        }
        setLoading(true);
        setError(null);
        try {
            console.log('MatchesPage: Calling getMatches API...');
            const data = await getMatches(currentPage - 1, 10);
            console.log('MatchesPage: API response received:', data);
            setMatches(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (err) {
            console.error("Failed to fetch matches:", err);
            setError(err.message || "Could not load potential collaborators.");
            setMatches([]);
            setTotalPages(0);
        } finally {
            setLoading(false);
        }
    }, [keycloakAuthenticated]);

    useEffect(() => {
        console.log('MatchesPage: useEffect for fetchMatches triggered. Page:', page);
        fetchMatches(page);
    }, [fetchMatches, page]);

    const handlePageChange = (event, value) => {
        setPage(value);
    };

    if (loading && matches.length === 0) {
        console.log('MatchesPage: Rendering loading spinner.');
        return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 5 }}><CircularProgress /></Box>;
    }

    return (
        <Container maxWidth="md">
            <Typography variant="h4" component="h1" gutterBottom sx={{ mt: 2, mb: 3, textAlign: 'center' }}>
                Potential Collaborators
            </Typography>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            {matches.length === 0 && !loading ? (
                <Typography textAlign="center" color="textSecondary" sx={{mt: 5}}>
                    No matches found at the moment. Try updating your profile or check back later!
                </Typography>
            ) : (
                <Paper elevation={1}>
                    <List disablePadding>
                        {matches.map((user, index) => (
                            <React.Fragment key={user.id}>
                                <UserListItem user={user} />
                                {index < matches.length - 1 && <Divider />}
                            </React.Fragment>
                        ))}
                    </List>
                </Paper>
            )}

            {totalPages > 1 && (
                <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3, mb: 2 }}>
                    <Pagination count={totalPages} page={page} onChange={handlePageChange} color="primary" />
                </Box>
            )}
        </Container>
    );
}

export default MatchesPage;