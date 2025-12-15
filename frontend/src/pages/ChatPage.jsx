import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate, Link as RouterLink } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import { getChatSummaries, getChatMessagesBetweenUsers } from '../services/api';
import keycloak from '../keycloak';

import SockJS from 'sockjs-client/dist/sockjs';
import { Stomp } from '@stomp/stompjs';

// MUI Components
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Avatar from '@mui/material/Avatar';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import SendIcon from '@mui/icons-material/Send';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Badge from '@mui/material/Badge';
import CircleIcon from '@mui/icons-material/Circle';
import Divider from '@mui/material/Divider';
import {alpha, styled} from '@mui/material/styles';
import ChatIcon from "@mui/icons-material/Chat";

const CHAT_SERVICE_WS_URL = import.meta.env.VITE_CHAT_SERVICE_WS_URL || 'http://192.168.1.112:8081/ws';

const MessageBubble = styled(Paper, {
    shouldForwardProp: (prop) => prop !== 'isSender',
})(({ theme, isSender }) => ({
    padding: theme.spacing(1, 1.5),
    marginBottom: theme.spacing(1),
    maxWidth: '70%',
    wordBreak: 'break-word',
    borderRadius: isSender ? '20px 20px 5px 20px' : '20px 20px 20px 5px',
    backgroundColor: isSender ? theme.palette.primary.main : theme.palette.grey[200],
    color: isSender ? theme.palette.primary.contrastText : theme.palette.text.primary,
    alignSelf: isSender ? 'flex-end' : 'flex-start',
}));

const TimestampText = styled(Typography)(({ theme, isSender }) => ({
    fontSize: '0.7rem',
    color: isSender ? alpha(theme.palette.primary.contrastText, 0.7) : theme.palette.text.secondary,
    textAlign: isSender ? 'right' : 'left',
    marginTop: theme.spacing(0.5),
}));


function ChatPage() {
    const stompClientRef = useRef(null);
    const { recipientId: recipientIdFromUrl } = useParams();
    const navigate = useNavigate();
    const { appUser, keycloakAuthenticated, keycloakInitialized } = useUser();

    const [stompClient, setStompClient] = useState(null);
    const [isConnected, setIsConnected] = useState(false);
    const [chatSummaries, setChatSummaries] = useState([]);
    const [onlineUsers, setOnlineUsers] = useState({});

    const [selectedChat, setSelectedChat] = useState(null);
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');

    const [loadingSummaries, setLoadingSummaries] = useState(true);
    const [loadingMessages, setLoadingMessages] = useState(false);
    const [initialChatLoad, setInitialChatLoad] = useState(true);
    const [error, setError] = useState(null);

    const messageAreaRef = useRef(null);

    const scrollToBottom = () => {
        if (messageAreaRef.current) {
            messageAreaRef.current.scrollTop = messageAreaRef.current.scrollHeight;
        }
    };

    useEffect(scrollToBottom, [messages]);


    // --- WebSocket Connection Logic ---
    useEffect(() => {
        if (keycloakAuthenticated && appUser?.id && keycloak.token && !stompClient) {
            console.log('ChatPage: Attempting WebSocket connection...');
            const socket = new SockJS(CHAT_SERVICE_WS_URL);
            const client = Stomp.over(socket);

            client.configure({
                connectHeaders: { Authorization: `Bearer ${keycloak.token}` },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
                onConnect: () => {
                    console.log('ChatPage: WebSocket Connected!');
                    setIsConnected(true);
                    setError(null);

                    client.subscribe(`/user/${appUser.id}/queue/messages`, (payload) => {
                        const notification = JSON.parse(payload.body);
                        console.log('ChatPage: New message received:', notification);
                        setMessages(prevMessages => {
                            if (prevMessages.find(msg => msg.id === notification.id)) return prevMessages;
                            return [...prevMessages, notification]
                        });
                        if (selectedChat && selectedChat.chatId === notification.chatId) {
                            client.publish({
                                destination: '/app/chat.markAsRead',
                                body: JSON.stringify({ chatId: notification.chatId })
                            });
                        } else {
                            setChatSummaries(prev => prev.map(summary =>
                                summary.chatId === notification.chatId
                                    ? { ...summary, unreadCount: (summary.unreadCount || 0) + 1 }
                                    : summary
                            ));
                        }
                    });

                    client.subscribe(`/user/${appUser.id}/queue/status-updates`, (payload) => {
                        const statusUpdate = JSON.parse(payload.body);
                        console.log('ChatPage: Message status update:', statusUpdate);
                        setMessages(prev => prev.map(msg =>
                            statusUpdate.messageIds.includes(msg.id)
                                ? { ...msg, status: statusUpdate.newStatus, ...(statusUpdate.newStatus === 'READ' && {readAt: statusUpdate.statusTimestamp}), ...(statusUpdate.newStatus === 'DELIVERED' && {deliveredAt: statusUpdate.statusTimestamp}) }
                                : msg
                        ));
                    });

                    client.subscribe('/topic/presence', (payload) => {
                        const presenceUpdate = JSON.parse(payload.body);
                        console.log('ChatPage: Presence update:', presenceUpdate);
                        setOnlineUsers(prev => ({...prev, [presenceUpdate.id]: presenceUpdate.status}));
                        setChatSummaries(prevSummaries => prevSummaries.map(summary =>
                            summary.chatPartner.id === presenceUpdate.id
                                ? { ...summary, chatPartner: { ...summary.chatPartner, status: presenceUpdate.status } }
                                : summary
                        ));
                    });

                    client.publish({
                        destination: '/app/user.addUser',
                        body: JSON.stringify({
                            userId: appUser.id,
                            username: appUser.username,
                            firstName: appUser.firstName,
                            lastName: appUser.lastName,
                        }),
                    });

                    fetchSummaries();
                },
                onDisconnect: () => {
                    console.log('ChatPage: WebSocket Disconnected!');
                    setIsConnected(false);
                },
                onStompError: (frame) => {
                    console.error('ChatPage: Broker reported error: ' + frame.headers['message']);
                    console.error('ChatPage: Additional details: ' + frame.body);
                    setError('WebSocket connection error. Trying to reconnect...');
                    setIsConnected(false);
                },
                onWebSocketError: (event) => {
                    console.error('ChatPage: WebSocket error observed:', event);
                    setError('WebSocket error. Please check connection or refresh.');
                    setIsConnected(false);
                }
            });

            client.activate();
            stompClientRef.current = client;
            setStompClient(client);
        }

        return () => {
            if (stompClient && stompClient.connected) {
                console.log('ChatPage: Disconnecting WebSocket on component unmount...');
                stompClient.publish({
                    destination: '/app/user.disconnectUser',
                    body: JSON.stringify({ userId: appUser?.id }),
                });
                stompClient.deactivate();
                console.log('ChatPage: WebSocket deactivated.');
                setIsConnected(false);
                setStompClient(null);
            }
        };
    }, [keycloakAuthenticated, appUser?.id, keycloak.token]);

    // --- Handle Chat Selection ---
    const handleSelectChat = useCallback(async (summary) => {
        console.log("Selected chat with:", summary.chatPartner.username, "Chat ID:", summary.chatId);
        if (selectedChat?.chatId === summary.chatId) return;

        setSelectedChat({
            chatId: summary.chatId,
            recipient: summary.chatPartner
        });
        setMessages([]);
        setNewMessage('');
        setLoadingMessages(true);
        setError(null);

        try {
            const historicalMessages = await getChatMessagesBetweenUsers(appUser.id, summary.chatPartner.id);
            setMessages(historicalMessages || []);

            if (summary.chatId && stompClient && stompClient.connected) {
                stompClient.publish({
                    destination: '/app/chat.markAsRead',
                    body: JSON.stringify({ chatId: summary.chatId })
                });
                setChatSummaries(prev => prev.map(s => s.chatId === summary.chatId ? {...s, unreadCount: 0} : s));
            }
        } catch (err) {
            console.error("Failed to fetch messages for selected chat:", err);
            setError(err.message || "Could not load messages for this chat.");
        } finally {
            setLoadingMessages(false);
            setTimeout(scrollToBottom, 0);
        }

        if (activeChatPollIntervalRef.current) {
            clearInterval(activeChatPollIntervalRef.current);
        }

        if (summary.chatPartner.id && appUser?.id) {
            activeChatPollIntervalRef.current = setInterval(async () => {
                console.log(`Polling messages for chat with ${summary.chatPartner.username}`);
                try {
                    if (selectedChatRef.current?.recipient?.id === summary.chatPartner.id) {
                        const freshMessages = await getChatMessagesBetweenUsers(appUser.id, summary.chatPartner.id);
                        setMessages(freshMessages || []);
                    } else {
                        clearInterval(activeChatPollIntervalRef.current);
                    }
                } catch (pollError) {
                    console.error("Error polling messages:", pollError);
                }
            }, 5000);
            console.log(`Polling started for chat with ${summary.chatPartner.username}`);
        }
    }, [appUser?.id, stompClient, selectedChat?.chatId]);

    const selectedChatRef = useRef(selectedChat);
    useEffect(() => {
        selectedChatRef.current = selectedChat;
    }, [selectedChat]);

    useEffect(() => {
        return () => {
            if (activeChatPollIntervalRef.current) {
                clearInterval(activeChatPollIntervalRef.current);
                console.log("Cleared active chat polling interval on unmount/chat change.");
            }
        };
    }, []);

    const CHAT_SUMMARIES_REFRESH_INTERVAL = 5000;
    const chatSummariesIntervalRef = useRef(null);

    // --- Fetch Initial Chat Summaries ---
    const fetchSummaries = useCallback(async (isPeriodicRefresh = false) => {
        if (!keycloakAuthenticated || !isConnected) {
            if (!isPeriodicRefresh) setLoadingSummaries(true);
            return;
        }
        if (!isPeriodicRefresh) setLoadingSummaries(true);
        // setError(null);

        console.log(isPeriodicRefresh ? 'ChatPage: Periodically refreshing chat summaries...' : 'ChatPage: Fetching initial chat summaries...');
        try {
            const summaries = await getChatSummaries();
            const sortedSummaries = (summaries || []).sort((a,b) => {
                const dateA = a.latestMessageTimestamp ? new Date(a.latestMessageTimestamp) : new Date(0);
                const dateB = b.latestMessageTimestamp ? new Date(b.latestMessageTimestamp) : new Date(0);
                return dateB - dateA;
            });

            setChatSummaries(prevSummaries => {
                return sortedSummaries;
            });

            setOnlineUsers(prevOnline => {
                const newOnline = {...prevOnline};
                (summaries || []).forEach(s => {
                    if (s.chatPartner?.id && s.chatPartner.status) {
                        newOnline[s.chatPartner.id] = s.chatPartner.status;
                    } else if (s.chatPartner?.id && !newOnline[s.chatPartner.id]) {
                        newOnline[s.chatPartner.id] = 'OFFLINE';
                    }
                });
                return newOnline;
            });


            if (recipientIdFromUrl && !initialChatLoad && summaries && summaries.length > 0) {
                const targetSummary = summaries.find(s => s.chatPartner.id === recipientIdFromUrl);
                if (targetSummary) {
                    handleSelectChat(targetSummary);
                }
                setInitialChatLoad(true);
            }

        } catch (err) {
            console.error("Failed to fetch chat summaries:", err);
            if (!isPeriodicRefresh) {
                setError(err.message || "Could not load your chats.");
            } else {
                console.warn("Periodic chat summary refresh failed:", err.message);
            }
        } finally {
            if (!isPeriodicRefresh) setLoadingSummaries(false);
        }
    }, [keycloakAuthenticated, isConnected, recipientIdFromUrl, initialChatLoad, handleSelectChat]);

    useEffect(() => {
        if (isConnected && appUser?.id) {
            fetchSummaries();

            if (chatSummariesIntervalRef.current) {
                clearInterval(chatSummariesIntervalRef.current);
            }
            chatSummariesIntervalRef.current = setInterval(() => {
                fetchSummaries(true);
            }, CHAT_SUMMARIES_REFRESH_INTERVAL);

            console.log('ChatPage: Chat summaries refresh interval started.');
        }
        return () => {
            if (chatSummariesIntervalRef.current) {
                clearInterval(chatSummariesIntervalRef.current);
                console.log('ChatPage: Chat summaries refresh interval cleared.');
            }
        };
    }, [isConnected, appUser?.id, fetchSummaries]);

    const activeChatPollIntervalRef = useRef(null);

    // --- Handle Sending Message ---
    const handleSendMessage = (event) => {
        event.preventDefault();
        if (newMessage.trim() && stompClientRef.current?.connected && selectedChat?.recipient?.id && appUser?.id) {
            const tempId = `temp-${Date.now()}`;
            const sentAtTime = new Date().toISOString();

            const optimisticMessage = {
                id: tempId,
                chatId: selectedChat.chatId || `temp-chat-${selectedChat.recipient.id}`,
                senderId: appUser.id,
                recipientId: selectedChat.recipient.id,
                content: newMessage.trim(),
                status: 'SENT',
                sentAt: sentAtTime,
            };

            setMessages(prevMessages => [...prevMessages, optimisticMessage]);
            scrollToBottom();

            const chatMessagePayload = {
                senderId: appUser.id,
                recipientId: selectedChat.recipient.id,
                content: newMessage.trim(),
            };

            stompClientRef.current.publish({
                destination: '/app/chat',
                body: JSON.stringify(chatMessagePayload),
            });

            setNewMessage('');
            fetchSummaries(true);
        }
    };

    if (!keycloakInitialized) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}><CircularProgress /><Typography sx={{ml:2}}>Initializing...</Typography></Box>;
    }
    if (!keycloakAuthenticated) {
        return <Container sx={{mt:3}}><Alert severity="warning">Please log in to access chat.</Alert></Container>;
    }
    if (!appUser) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}><CircularProgress /><Typography sx={{ml:2}}>Loading user data...</Typography></Box>;
    }


    return (
        <Box sx={{ display: 'flex', height: 'calc(100vh - 64px)', overflow: 'hidden' }}>
            {/* Left Pane: Chat List */}
            <Paper
                elevation={3}
                sx={{
                    width: { xs: '100%', sm: 300, md: 350 },
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    borderRight: (theme) => `1px solid ${theme.palette.divider}`,
                }}
            >
                <Box sx={{ p: 2, borderBottom: (theme) => `1px solid ${theme.palette.divider}` }}>
                    <Typography variant="h6">My Chats</Typography>
                </Box>
                {loadingSummaries ? <CircularProgress sx={{m:'auto'}}/> :
                    error && chatSummaries.length === 0 ? <Alert severity="error" sx={{m:1}}>{error}</Alert> :
                        chatSummaries.length === 0 ? <Typography sx={{p:2, textAlign:'center', color:'text.secondary'}}>No active chats. Find users to connect with!</Typography> :
                            <List sx={{ overflowY: 'auto', flexGrow: 1 }}>
                                {chatSummaries.map((summary) => (
                                    <ListItemButton
                                        key={summary.chatPartner.id}
                                        selected={selectedChat?.recipient?.id === summary.chatPartner.id}
                                        onClick={() => handleSelectChat(summary)}
                                    >
                                        <ListItemAvatar>
                                            <Badge
                                                overlap="circular"
                                                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                                                variant="dot"
                                                invisible={onlineUsers[summary.chatPartner.id] !== 'ONLINE'}
                                                sx={{
                                                    '& .MuiBadge-badge': {
                                                        backgroundColor: '#44b700',
                                                        color: '#44b700',
                                                        boxShadow: (theme) => `0 0 0 2px ${theme.palette.background.paper}`,
                                                    }
                                                }}
                                            >
                                                <Avatar>
                                                    {`${summary.chatPartner.firstName ? summary.chatPartner.firstName[0] : ''}${summary.chatPartner.lastName ? summary.chatPartner.lastName[0] : ''}`.toUpperCase() || summary.chatPartner.username[0].toUpperCase()}
                                                </Avatar>
                                            </Badge>
                                        </ListItemAvatar>
                                        <ListItemText
                                            primary={`${summary.chatPartner.firstName || ''} ${summary.chatPartner.lastName || ''}`.trim() || summary.chatPartner.username}
                                            secondary={`@${summary.chatPartner.username}`}
                                        />
                                        {summary.unreadCount > 0 && (
                                            <Badge badgeContent={summary.unreadCount} color="error" />
                                        )}
                                    </ListItemButton>
                                ))}
                            </List>
                }
            </Paper>

            {/* Right Pane: Message Area & Input */}
            <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', height: '100%', p: {xs:1, sm:2} }}>
                {selectedChat ? (
                    <>
                        <Paper elevation={1} sx={{ p: 1.5, mb: 1, display:'flex', alignItems:'center' }}>
                            <Avatar sx={{mr:1.5, width:32, height:32}}>{`${selectedChat.recipient.firstName ? selectedChat.recipient.firstName[0] : ''}${selectedChat.recipient.lastName ? selectedChat.recipient.lastName[0] : ''}`.toUpperCase() || selectedChat.recipient.username[0].toUpperCase()}</Avatar>
                            <Typography variant="h6">{`${selectedChat.recipient.firstName || ''} ${selectedChat.recipient.lastName || ''}`.trim() || selectedChat.recipient.username}</Typography>
                            {onlineUsers[selectedChat.recipient.id] === 'ONLINE' && <CircleIcon color="success" sx={{fontSize:12, ml:1}}/>}
                        </Paper>
                        <Box
                            ref={messageAreaRef}
                            sx={{
                                flexGrow: 1,
                                overflowY: 'auto',
                                p: 2,
                                display: 'flex',
                                flexDirection: 'column',
                                gap: 1,
                                backgroundColor: (theme) => theme.palette.background.default,
                                borderRadius: 1
                            }}
                        >
                            {loadingMessages && <CircularProgress sx={{m:'auto'}} />}
                            {!loadingMessages && error && <Alert severity="error">{error}</Alert>}
                            {!loadingMessages && messages.length === 0 && !error && (
                                <Typography sx={{textAlign:'center', color:'text.secondary', mt:5}}>
                                    No messages yet. Start the conversation!
                                </Typography>
                            )}
                            {messages.map((msg) => (
                                <MessageBubble key={msg.id} elevation={1} isSender={msg.senderId === appUser.id}>
                                    <Typography variant="body2">{msg.content}</Typography>
                                    <TimestampText variant="caption" isSender={msg.senderId === appUser.id}>
                                        {new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                        {msg.senderId === appUser.id && msg.status === 'SENT' && ' ✓'}
                                        {msg.senderId === appUser.id && msg.status === 'DELIVERED' && ' ✓✓'}
                                        {msg.senderId === appUser.id && msg.status === 'READ' && <span style={{color: '#4fc3f7'}}>✓✓</span>}
                                    </TimestampText>
                                </MessageBubble>
                            ))}
                        </Box>
                        <Box component="form" onSubmit={handleSendMessage} sx={{ display: 'flex', mt: 1, gap: 1 }}>
                            <TextField
                                fullWidth
                                variant="outlined"
                                size="small"
                                placeholder="Type a message..."
                                value={newMessage}
                                onChange={(e) => setNewMessage(e.target.value)}
                                disabled={!isConnected}
                            />
                            <Button type="submit" variant="contained" endIcon={<SendIcon />} disabled={!isConnected || !newMessage.trim()}>
                                Send
                            </Button>
                        </Box>
                    </>
                ) : (
                    <Box sx={{ display: 'flex', flexDirection:'column', justifyContent: 'center', alignItems: 'center', height: '100%', textAlign:'center' }}>
                        <ChatIcon sx={{fontSize: 60, color: 'text.disabled', mb:2}}/>
                        <Typography variant="h6" color="text.secondary">Select a chat to start messaging</Typography>
                        <Typography variant="body2" color="text.secondary">Your conversations will appear here.</Typography>
                    </Box>
                )}
            </Box>
        </Box>
    );
}

export default ChatPage;