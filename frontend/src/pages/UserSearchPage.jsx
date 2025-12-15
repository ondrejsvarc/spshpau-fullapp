import React, { useState, useEffect, useCallback } from 'react';
import { searchUsers, getAllGenres, getAllSkills } from '../services/api';
import UserListItem from '../components/UserListItem';
import { useUser } from '../contexts/UserContext';

import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import List from '@mui/material/List';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Pagination from '@mui/material/Pagination';
import Divider from '@mui/material/Divider';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Checkbox from '@mui/material/Checkbox';
import ListItemText from '@mui/material/ListItemText';
import OutlinedInput from '@mui/material/OutlinedInput';
import FormGroup from '@mui/material/FormGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import Chip from '@mui/material/Chip';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import {/*useNavigate,*/ useSearchParams} from "react-router-dom";

const experienceLevels = ['', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];

const renderSelectFilter = (name, label, currentValue, onChangeHandler, options) => {
    return (
        <FormControl fullWidth margin="dense" sx={{ minWidth: 200 }}>
            <InputLabel id={`${name}-label`}>{label}</InputLabel>
            <Select
                name={name}
                labelId={`${name}-label`}
                value={currentValue}
                label={label}
                onChange={onChangeHandler}
            >
                {options.map(opt => (
                    <MenuItem key={opt.value} value={opt.value}>
                        {opt.label}
                    </MenuItem>
                ))}
            </Select>
        </FormControl>
    );
};

function UserSearchPage() {
    console.log('UserSearchPage: Component rendering/mounting.');
    const [searchParams, setSearchParams] = useSearchParams();
    const { keycloakAuthenticated } = useUser();
    const [searchResults, setSearchResults] = useState([]);
    const [searchLoading, setSearchLoading] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);

    const [filters, setFilters] = useState({
        searchTerm: searchParams.get('searchTerm') || '',
        genreIds: searchParams.getAll('genreIds') || [],
        skillIds: searchParams.getAll('skillIds') || [],
        hasArtistProfile: searchParams.get('hasArtistProfile') === 'true' ? true : null,
        hasProducerProfile: searchParams.get('hasProducerProfile') === 'true' ? true : null,
        artistExperienceLevel: searchParams.get('artistExperienceLevel') || '',
        artistAvailability: searchParams.get('artistAvailability') === 'true' ? true : null,
        producerExperienceLevel: searchParams.get('producerExperienceLevel') || '',
        producerAvailability: searchParams.get('producerAvailability') === null ? null : searchParams.get('producerAvailability') === 'true',
    });

    const [availableGenres, setAvailableGenres] = useState([]);
    const [availableSkills, setAvailableSkills] = useState([]);
    const [optionsLoading, setOptionsLoading] = useState(true);

    const [advancedFiltersExpanded, setAdvancedFiltersExpanded] = useState(
        !!filters.searchTerm ||
        Object.values(filters).some(v => Array.isArray(v) ? v.length > 0 : (typeof v === 'boolean' && v !== null) || (typeof v === 'string' && v !== ''))
    );

    console.log('UserSearchPage: Initial state - keycloakAuthenticated:', keycloakAuthenticated, 'URL searchTerm:', searchParams.get('searchTerm'));

    useEffect(() => {
        const fetchFilterOptions = async () => {
            setOptionsLoading(true);
            try {
                const [genresData, skillsData] = await Promise.all([
                    getAllGenres(0, 100),
                    getAllSkills(0, 100)
                ]);
                setAvailableGenres(genresData.content || []);
                setAvailableSkills(skillsData.content || []);
            } catch (err) {
                console.error("Failed to fetch filter options:", err);
                setError("Could not load filter options.");
            } finally {
                setOptionsLoading(false);
            }
        };
        fetchFilterOptions();
    }, []);

    const performSearch = useCallback(async (currentPage, currentFilters) => {
        console.log('UserSearchPage: performSearch called. Filters:', currentFilters, 'Page:', currentPage, 'Auth:', keycloakAuthenticated);
        if (!keycloakAuthenticated) {
            console.log('UserSearchPage: Not authenticated, skipping search.');
            setSearchLoading(false);
            setSearchResults([]);
            setTotalPages(0);
            return;
        }
        setSearchLoading(true);
        setError(null);
        const activeFiltersForApi = {};

        for (const key in currentFilters) {
            const value = currentFilters[key];
            if (value === null || value === '' || (Array.isArray(value) && value.length === 0)) {
                continue;
            }

            switch (key) {
                case 'searchTerm':
                    activeFiltersForApi.searchTerm = value;
                    break;
                case 'genreIds':
                    activeFiltersForApi.genreIds = value;
                    break;
                case 'skillIds':
                    activeFiltersForApi.skillIds = value;
                    break;
                case 'hasArtistProfile':
                    activeFiltersForApi.hasArtist = String(value);
                    break;
                case 'hasProducerProfile':
                    activeFiltersForApi.hasProducer = String(value);
                    break;
                case 'artistExperienceLevel':
                    activeFiltersForApi.artistExperienceLevel = value;
                    break;
                case 'artistAvailability':
                    activeFiltersForApi.artistAvailability = String(value);
                    break;
                case 'producerExperienceLevel':
                    activeFiltersForApi.producerExperienceLevel = value;
                    break;
                case 'producerAvailability':
                    activeFiltersForApi.producerAvailability = String(value);
                    break;
                default:
                    console.warn(`Unhandled filter key: ${key}`);
                    activeFiltersForApi[key] = value;
            }
        }
        console.log('UserSearchPage: Constructed activeFiltersForApi FOR API CALL:', activeFiltersForApi);

        try {
            const data = await searchUsers(activeFiltersForApi, currentPage - 1, 10);
            console.log('UserSearchPage: API response received:', data);
            setSearchResults(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (err) {
            console.error("Failed to search users:", err);
            setError(err.message || "Could not perform search.");
            setSearchResults([]);
            setTotalPages(0);
        } finally {
            setSearchLoading(false);
        }
    }, [keycloakAuthenticated]);

    useEffect(() => {
        if (!optionsLoading) {
            performSearch(page, filters);
        }
    }, [filters, page, performSearch, optionsLoading]);

    useEffect(() => {
        console.log('UserSearchPage: useEffect for performSearch triggered. Filters:', filters, 'Page:', page, 'OptionsLoading:', optionsLoading);
        if (!optionsLoading) {
            performSearch(page, filters);
        }
    }, [filters, page, performSearch, optionsLoading]);

    const handleFilterChange = (event) => {
        const { name, value } = event.target;
        let processedValue;

        if (name === 'hasArtistProfile' || name === 'hasProducerProfile' || name === 'artistAvailability' || name === 'producerAvailability') {
            if (value === "any_option_value_for_boolean_null") {
                processedValue = null;
            } else if (value === "true_option_value") {
                processedValue = true;
            } else if (value === "false_option_value") {
                processedValue = false;
            } else {
                processedValue = value;
            }
        }
        else if (name === 'artistExperienceLevel' || name === 'producerExperienceLevel') {
            if (value === "") {
                processedValue = "";
            } else {
                processedValue = value;
            }
        }
        else {
            processedValue = value;
        }

        setFilters(prev => ({ ...prev, [name]: processedValue }));
        setPage(1);
    };

    const handleMultiSelectChange = (event, fieldName) => {
        const { target: { value } } = event;
        setFilters(prev => ({
            ...prev,
            [fieldName]: typeof value === 'string' ? value.split(',') : value,
        }));
        setPage(1);
    };

    const handleSearchSubmit = (event) => {
        event.preventDefault();
        setPage(1);
        const newSearchParams = new URLSearchParams();
        if (filters.searchTerm) newSearchParams.set('searchTerm', filters.searchTerm);
        filters.genreIds.forEach(id => newSearchParams.append('genreIds', id));
        filters.skillIds.forEach(id => newSearchParams.append('skillIds', id));
        if (filters.hasArtistProfile !== null) newSearchParams.set('hasArtistProfile', filters.hasArtistProfile.toString());
        if (filters.hasProducerProfile !== null) newSearchParams.set('hasProducerProfile', filters.hasProducerProfile.toString());
        if (filters.artistExperienceLevel) newSearchParams.set('artistExperienceLevel', filters.artistExperienceLevel);
        if (filters.artistAvailability !== null) newSearchParams.set('artistAvailability', filters.artistAvailability.toString());
        if (filters.producerExperienceLevel) newSearchParams.set('producerExperienceLevel', filters.producerExperienceLevel);
        if (filters.producerAvailability !== null) newSearchParams.set('producerAvailability', filters.producerAvailability.toString());
        setSearchParams(newSearchParams, { replace: true });
    };

    const handlePageChange = (event, value) => {
        setPage(value);
    };

    const profileExistenceOptions = [
        { value: "any_option_value_for_boolean_null", label: "Any" },
        { value: "true_option_value", label: "Yes" }
    ];

    const availabilityOptions = [
        { value: "any_option_value_for_boolean_null", label: "Any" },
        { value: "true_option_value", label: "Yes" }
    ];

    const pageLoading = optionsLoading || (searchLoading && searchResults.length === 0);

    if (pageLoading) {
        console.log('UserSearchPage: Rendering main loading spinner (options or initial search).');
        return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 5 }}><CircularProgress /></Box>;
    }

    return (
        <Container maxWidth="lg">
            <Typography variant="h4" component="h1" gutterBottom sx={{ mt: 2, mb: 1, textAlign: 'center' }}>
                Search Users
            </Typography>

            <Paper elevation={2} sx={{ p: {xs: 2, sm:3}, mb: 3 }}>
                <Box component="form" onSubmit={handleSearchSubmit}>
                    <TextField
                        fullWidth
                        label="Search by Username, Name..."
                        name="searchTerm"
                        value={filters.searchTerm}
                        onChange={handleFilterChange}
                        margin="normal"
                        variant="outlined"
                    />
                    <Accordion
                        sx={{mt:1, mb:1}}
                        expanded={advancedFiltersExpanded}
                        onChange={() => setAdvancedFiltersExpanded(!advancedFiltersExpanded)}
                    >
                        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                            <Typography>Advanced Filters</Typography>
                        </AccordionSummary>
                        <AccordionDetails>
                            <Grid container spacing={2} alignItems="flex-start">
                                {/* Genre Select */}
                                <Grid item xs={12} sm={6} md={4}>
                                    <FormControl fullWidth margin="dense" disabled={optionsLoading} sx={{ minWidth: 200 }}>
                                        <InputLabel id="genre-select-label">Genres</InputLabel>
                                        <Select
                                            labelId="genre-select-label"
                                            multiple
                                            name="genreIds"
                                            value={filters.genreIds}
                                            onChange={(e) => handleMultiSelectChange(e, 'genreIds')}
                                            input={<OutlinedInput label="Genres" />}
                                            renderValue={(selected) => (
                                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                                                    {selected.map((value) => {
                                                        const genre = availableGenres.find(g => g.id === value);
                                                        return <Chip key={value} label={genre ? genre.name : value} size="small"/>;
                                                    })}
                                                </Box>
                                            )}
                                        >
                                            {availableGenres.map((genre) => (
                                                <MenuItem key={genre.id} value={genre.id}>
                                                    <Checkbox checked={filters.genreIds.includes(genre.id)} />
                                                    <ListItemText primary={genre.name} />
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Grid>
                                {/* Skill Select */}
                                <Grid item xs={12} sm={6} md={4}>
                                    <FormControl fullWidth margin="dense" disabled={optionsLoading} sx={{ minWidth: 200 }}>
                                        <InputLabel id="skill-select-label">Skills</InputLabel>
                                        <Select
                                            labelId="skill-select-label"
                                            multiple
                                            name="skillIds"
                                            value={filters.skillIds}
                                            onChange={(e) => handleMultiSelectChange(e, 'skillIds')}
                                            input={<OutlinedInput label="Skills" />}
                                            renderValue={(selected) => (
                                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                                                    {selected.map((value) => {
                                                        const skill = availableSkills.find(s => s.id === value);
                                                        return <Chip key={value} label={skill ? skill.name : value} size="small"/>;
                                                    })}
                                                </Box>
                                            )}
                                        >
                                            {availableSkills.map((skill) => (
                                                <MenuItem key={skill.id} value={skill.id}>
                                                    <Checkbox checked={filters.skillIds.includes(skill.id)} />
                                                    <ListItemText primary={skill.name} />
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Grid>
                                {/* Profile Existence Filters */}
                                <Grid item xs={12} sm={6} md={4}>
                                    {renderSelectFilter('hasArtistProfile', 'Has Artist Profile?', filters.hasArtistProfile === null ? "any_option_value_for_boolean_null" : (filters.hasArtistProfile ? "true_option_value" : "false_option_value"), handleFilterChange, profileExistenceOptions)}
                                </Grid>
                                <Grid item xs={12} sm={6} md={4}>
                                    {renderSelectFilter('hasProducerProfile', 'Has Producer Profile?', filters.hasProducerProfile === null ? "any_option_value_for_boolean_null" : (filters.hasProducerProfile ? "true_option_value" : "false_option_value"), handleFilterChange, profileExistenceOptions)}
                                </Grid>
                                <Grid item xs={12} sm={6} md={4}>
                                    <FormControl fullWidth margin="dense" sx={{ minWidth: 200 }}>
                                        <InputLabel id="artistExp-label">Artist Experience</InputLabel>
                                        <Select labelId="artistExp-label" name="artistExperienceLevel" value={filters.artistExperienceLevel} label="Artist Experience" onChange={handleFilterChange}>
                                            {experienceLevels.map(level => <MenuItem key={level} value={level}>{level || 'Any'}</MenuItem>)}
                                        </Select>
                                    </FormControl>
                                </Grid>
                                <Grid item xs={12} sm={6} md={4}>
                                    {renderSelectFilter('artistAvailability', 'Artist Available?', filters.artistAvailability === null ? "any_option_value_for_boolean_null" : (filters.artistAvailability ? "true_option_value" : "false_option_value"), handleFilterChange, availabilityOptions)}
                                </Grid>
                                <Grid item xs={12} sm={6} md={4}>
                                    <FormControl fullWidth margin="dense" sx={{ minWidth: 200 }}>
                                        <InputLabel id="producerExp-label">Producer Experience</InputLabel>
                                        <Select labelId="producerExp-label" name="producerExperienceLevel" value={filters.producerExperienceLevel} label="Producer Experience" onChange={handleFilterChange}>
                                            {experienceLevels.map(level => <MenuItem key={level} value={level}>{level || 'Any'}</MenuItem>)}
                                        </Select>
                                    </FormControl>
                                </Grid>
                                <Grid item xs={12} sm={6} md={4}>
                                    {renderSelectFilter('producerAvailability', 'Producer Available?', filters.producerAvailability === null ? "any_option_value_for_boolean_null" : (filters.producerAvailability ? "true_option_value" : "false_option_value"), handleFilterChange, availabilityOptions)}
                                </Grid>
                            </Grid>
                        </AccordionDetails>
                    </Accordion>
                    <Button type="submit" variant="contained" sx={{ mt: 2, mb:1 }} disabled={loading || optionsLoading}>
                        {(loading && !optionsLoading) ? <CircularProgress size={24} /> : "Search"}
                    </Button>
                </Box>
            </Paper>

            {/* Results Display */}
            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
            {loading && searchResults.length === 0 && !optionsLoading &&
                <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}><CircularProgress /></Box>
            }
            {!loading && searchResults.length === 0 && !error && (
                <Typography textAlign="center" color="textSecondary" sx={{mt: 5}}>
                    No users found matching your criteria. Try adjusting your filters or perform an empty search to see all users.
                </Typography>
            )}
            {searchResults.length > 0 && (
                <Paper elevation={1} sx={{mt:2}}>
                    <List disablePadding>
                        {searchResults.map((user, index) => (
                            <React.Fragment key={user.id}>
                                <UserListItem user={user} />
                                {index < searchResults.length - 1 && <Divider />}
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

export default UserSearchPage;