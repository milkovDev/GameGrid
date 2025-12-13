import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserDTO } from '../../types/UserDTO';
import { Button, Card } from 'react-bootstrap';
import { imageApi } from '../../api/imageApi';
import { useAuth } from '../../contexts/AuthContext';
import { useUser } from '../../contexts/UserContext';
import { followUser, unfollowUser } from '../../api/userApi';

interface Props {
    user: UserDTO;
    theme?: 'dark' | 'light';
    showProfileButton?: boolean;
}

const UserCard: React.FC<Props> = ({ user, theme = 'dark', showProfileButton = true }) => {
    const navigate = useNavigate();
    const { token, userId: currentUserId } = useAuth();
    const { userData, follow, unfollow } = useUser();
    
    const isOwnProfile = user.id === currentUserId;
    const isFollowing = userData?.graphData.following?.has(user.id) || false;
    const [optimisticFollowing, setOptimisticFollowing] = useState(isFollowing);

    useEffect(() => {
        setOptimisticFollowing(isFollowing);
    }, [isFollowing]);

    const handleProfileClick = () => {
        navigate(`/users/${user.id}`);
    };

    const handleFollowToggle = async () => {
        if (!token) return;

        const wasFollowing = optimisticFollowing;
        setOptimisticFollowing(!wasFollowing);

        if (wasFollowing) {
            unfollow(user.id);
        } else {
            follow(user.id);
        }

        try {
            if (wasFollowing) {
                await unfollowUser(token, user.id);
            } else {
                await followUser(token, user.id);
            }
        } catch (err) {
            console.error('Failed to toggle follow', err);
            setOptimisticFollowing(wasFollowing);
            if (wasFollowing) {
                follow(user.id);
            } else {
                unfollow(user.id);
            }
        }
    };

    const textClass = theme === 'light' ? 'text-dark' : 'text-light';
    const bgClass = theme === 'light' ? 'bg-light' : 'bg-dark';
    const buttonVariant = theme === 'light' ? 'outline-dark' : 'outline-light';

    return (
        <Card className={`mb-3 ${textClass} ${bgClass}`}>
            <Card.Body className="d-flex align-items-center">
                <img
                    src={imageApi.getAvatarImage(user.avatarUrl)}
                    style={{ width: '3.125rem', height: '3.125rem', borderRadius: '50%', objectFit: 'cover', marginRight: '1rem' }}
                />
                <div style={{ flex: 1 }}>
                    <Card.Title style={{ wordBreak: 'break-word', overflowWrap: 'break-word' }}>{user.displayName}</Card.Title>
                </div>
                <div>
                    {isOwnProfile ? (
                        <span className={`badge ${theme === 'light' ? 'bg-secondary' : 'bg-light text-dark'}`}>Me</span>
                    ) : (
                        <>
                            {showProfileButton && (
                                <Button variant={buttonVariant} size="sm" className="me-2" onClick={handleProfileClick}>Profile</Button>
                            )}
                            <Button 
                                variant={optimisticFollowing ? "outline-danger" : buttonVariant} 
                                size="sm" 
                                onClick={handleFollowToggle}
                            >
                                {optimisticFollowing ? "Unfollow" : "Follow"}
                            </Button>
                        </>
                    )}
                </div>
            </Card.Body>
        </Card>
    );
};

export default UserCard;