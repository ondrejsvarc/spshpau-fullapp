import React, { useState, useEffect, useCallback } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { getMyBlockedUsers, unblockUser } from '../services/api';
import { useUser } from '../contexts/UserContext';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Avatar from '@mui/material/Avatar';
import ListItemText from '@mui/material/ListItemText';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Divider from '@mui/material/Divider';
import Pagination from '@mui/material/Pagination';
import BlockIcon from '@mui/icons-material/Block';

function BlocksPage() {
  const { keycloakAuthenticated } = useUser();
  const [blockedUsers, setBlockedUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [actionLoading, setActionLoading] = useState({});

  const fetchBlockedUsers = useCallback(async (currentPage) => {
    if (!keycloakAuthenticated) return;
    setLoading(true);
    setError(null);
    try {
      const data = await getMyBlockedUsers(currentPage - 1, 10);
      setBlockedUsers(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      console.error("Failed to fetch blocked users:", err);
      setError(err.message || "Could not load blocked users.");
    } finally {
      setLoading(false);
    }
  }, [keycloakAuthenticated]);

  useEffect(() => {
    fetchBlockedUsers(page);
  }, [fetchBlockedUsers, page]);

  const handleUnblock = async (userIdToUnblock) => {
    setActionLoading(prev => ({ ...prev, [userIdToUnblock]: true }));
    try {
      await unblockUser(userIdToUnblock);
      // Refresh the list
      fetchBlockedUsers(page);
      // Optionally show a success message
    } catch (err) {
      console.error(`Failed to unblock user ${userIdToUnblock}:`, err);
      setError(`Failed to unblock user: ${err.message}`);
    } finally {
       setActionLoading(prev => ({ ...prev, [userIdToUnblock]: false }));
    }
  };

  const handlePageChange = (event, value) => {
    setPage(value);
  };

  if (loading && blockedUsers.length === 0) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 5 }}><CircularProgress /></Box>;
  }

  return (
    <Container maxWidth="md">
      <Typography variant="h4" component="h1" gutterBottom sx={{ mt: 2, mb: 3, textAlign: 'center' }}>
        My Blocked Users
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {blockedUsers.length === 0 && !loading ? (
        <Typography textAlign="center" color="textSecondary">You haven't blocked any users.</Typography>
      ) : (
        <List sx={{ bgcolor: 'background.paper', borderRadius: 1, boxShadow: 1 }}>
          {blockedUsers.map((user, index) => (
            <React.Fragment key={user.id}>
              <ListItem
                secondaryAction={
                  <Button
                    variant="outlined"
                    color="secondary"
                    size="small"
                    startIcon={<BlockIcon />}
                    onClick={() => handleUnblock(user.id)}
                    disabled={actionLoading[user.id]}
                  >
                    {actionLoading[user.id] ? <CircularProgress size={20}/> : "Unblock"}
                  </Button>
                }
              >
                <ListItemAvatar>
                  <Avatar component={RouterLink} to={`/users/${user.id}`}>
                    {`${user.firstName ? user.firstName[0] : ''}${user.lastName ? user.lastName[0] : ''}`.toUpperCase() || user.username[0].toUpperCase()}
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
                  secondary={`@${user.username}`}
                />
              </ListItem>
              {index < blockedUsers.length - 1 && <Divider variant="inset" component="li" />}
            </React.Fragment>
          ))}
        </List>
      )}

      {totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
          <Pagination count={totalPages} page={page} onChange={handlePageChange} color="primary" />
        </Box>
      )}
    </Container>
  );
}

export default BlocksPage;