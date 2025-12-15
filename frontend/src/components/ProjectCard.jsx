import React from 'react';
import { useNavigate } from 'react-router-dom';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardActions from '@mui/material/CardActions';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import FolderIcon from '@mui/icons-material/Folder';

const MAX_DESC_LENGTH = 100;

function ProjectCard({ project }) {
    const navigate = useNavigate();

    if (!project) return null;

    const ownerName = `${project.owner?.firstName || ''} ${project.owner?.lastName || ''}`.trim() || project.owner?.username || 'N/A';
    const descriptionShort = project.description
        ? (project.description.length > MAX_DESC_LENGTH ? `${project.description.substring(0, MAX_DESC_LENGTH)}...` : project.description)
        : 'No description available.';

    const handleCardClick = () => {
        navigate(`/projects/${project.id}`);
    };

    return (
        <Card
            sx={{
                minWidth: 275,
                maxWidth: 300,
                m: 1,
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                height: '100%',
                cursor: 'pointer',
                transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 6,
                }
            }}
            onClick={handleCardClick}
            elevation={3}
        >
            <CardContent sx={{ flexGrow: 1 }}>
                <Box display="flex" alignItems="center" mb={1}>
                    <FolderIcon color="primary" sx={{mr: 1}}/>
                    <Typography variant="h6" component="div" noWrap title={project.title}>
                        {project.title}
                    </Typography>
                </Box>
                <Typography sx={{ mb: 1.5 }} color="text.secondary">
                    Owner: {ownerName}
                </Typography>
                <Typography variant="body2" sx={{
                    height: 60,
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    display: "-webkit-box",
                    WebkitLineClamp: 3,
                    WebkitBoxOrient: "vertical"
                }}>
                    {descriptionShort}
                </Typography>
            </CardContent>
            {/* Optional CardActions if you want buttons directly on the card
      <CardActions>
        <Button size="small" onClick={handleCardClick}>View Details</Button>
      </CardActions>
      */}
        </Card>
    );
}

export default ProjectCard;