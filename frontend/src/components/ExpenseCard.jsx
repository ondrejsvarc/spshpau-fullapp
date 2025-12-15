import React from 'react';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardActions from '@mui/material/CardActions';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import DeleteIcon from '@mui/icons-material/Delete';
import Box from '@mui/material/Box';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';

function ExpenseCard({ expense, currency, onDelete }) {
    if (!expense) return null;

    return (
        <Box sx={{ flexShrink: 0, p: 1 }}>
            <Card sx={{ width: 270, height: '100%', display: 'flex', flexDirection: 'column' }} elevation={2}>
                <CardContent sx={{ flexGrow: 1 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <AttachMoneyIcon color="action" sx={{ mr: 1 }} />
                        <Typography variant="h6">
                            {expense.amount?.toFixed(2)} {currency || ''}
                        </Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                        Date: {new Date(expense.date).toLocaleDateString()}
                    </Typography>
                    <Typography variant="body2" sx={{
                        height: 60,
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                        display: "-webkit-box",
                        WebkitLineClamp: 3,
                        WebkitBoxOrient: "vertical"
                    }}>
                        Comment: {expense.comment || 'N/A'}
                    </Typography>
                </CardContent>
                <CardActions sx={{ justifyContent: 'flex-end' }}>
                    <IconButton aria-label="delete expense" onClick={() => onDelete(expense.id)} color="error" size="small">
                        <DeleteIcon />
                    </IconButton>
                </CardActions>
            </Card>
        </Box>
    );
}

export default ExpenseCard;