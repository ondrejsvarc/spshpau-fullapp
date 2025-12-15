import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Avatar from '@mui/material/Avatar';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Chip from '@mui/material/Chip';
import Stack from '@mui/material/Stack';
import PersonIcon from '@mui/icons-material/Person';
import MusicNoteIcon from '@mui/icons-material/MusicNote';

function UserListItem({ user }) {
    if (!user) return null;

    const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username;

    return (
        <ListItem
            alignItems="flex-start"
            button
            component={RouterLink}
            to={`/users/${user.id}`}
            sx={{ '&:hover': { backgroundColor: 'action.hover' } }}
        >
            <ListItemAvatar>
                <Avatar>
                    {`${user.firstName ? user.firstName[0] : ''}${user.lastName ? user.lastName[0] : ''}`.toUpperCase() || (user.username ? user.username[0].toUpperCase() : 'U')}
                </Avatar>
            </ListItemAvatar>
            <ListItemText
                primary={fullName}
                secondary={
                    <Box component="span">
                        <Typography
                            sx={{ display: 'block' }}
                            component="span"
                            variant="body2"
                            color="text.primary"
                        >
                            {user.username}
                        </Typography>
                        {user.location && (
                            <Typography component="span" variant="body2" color="text.secondary" sx={{ display: 'block' }}>
                                Location: {user.location}
                            </Typography>
                        )}
                        <Stack direction="row" spacing={1} sx={{ mt: 0.5 }}>
                            {user.artistProfile && (
                                <Chip icon={<PersonIcon />} label="Artist" size="small" variant="outlined" color="primary" />
                            )}
                            {user.producerProfile && (
                                <Chip icon={<MusicNoteIcon />} label="Producer" size="small" variant="outlined" color="secondary" />
                            )}
                        </Stack>
                    </Box>
                }
                secondaryTypographyProps={{ component: 'div' }}
            />
        </ListItem>
    );
}

export default UserListItem;