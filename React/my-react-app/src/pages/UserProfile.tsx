

import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useMessages } from '../contexts/MessageContext';
import ProfileInfo from '../components/users/ProfileInfo';
import GameListManager from '../components/ugles/GameListManager';
import ProfileActions from '../components/users/ProfileActions';
import { UserDTO } from '../types/UserDTO';
import { getById, followUser, unfollowUser } from '../api/userApi';
import UserConnectionsModal from '../components/users/UserConnectionsModal';
import { useUser } from '../contexts/UserContext';
import '../styles/ProfileStyles.css';

const UserProfile: React.FC = () => {
  const { token, userId: currentUserId } = useAuth();
  const { userData, follow, unfollow } = useUser();
  const { activateChat, selectChat, openChatBox } = useMessages();
  const { userId } = useParams<{ userId: string }>();
  
  const [user, setUser] = useState<UserDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [showFollowingModal, setShowFollowingModal] = useState(false);
  const [showFollowersModal, setShowFollowersModal] = useState(false);

  useEffect(() => {
    if (!token || !userId) {
      setLoading(false);
      return;
    }
    
    const fetchUser = async () => {
      try {
        const data = await getById(token, userId);
        setUser(data);
        setIsFollowing(userData?.graphData.following?.has(userId) || false);
      } catch (err) {
        setError('Failed to load user profile');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    
    fetchUser();
  }, [token, userId, userData?.graphData.following]);

  useEffect(() => {
    setShowFollowingModal(false);
    setShowFollowersModal(false);
  }, [userId]);

  const handleFollowToggle = async () => {
    if (!token || !userId) return;

    const wasFollowing = isFollowing;
    setIsFollowing(!wasFollowing);

    if (wasFollowing) {
      unfollow(userId);
    } else {
      follow(userId);
    }

    try {
      if (wasFollowing) {
        await unfollowUser(token, userId);
      } else {
        await followUser(token, userId);
      }
    } catch (err) {
      console.error('Failed to toggle follow', err);
      setIsFollowing(wasFollowing);
      if (wasFollowing) {
        follow(userId);
      } else {
        unfollow(userId);
      }
    }
  };

  const handleMessageClick = async () => {
    if (!userId) return;
    await activateChat(userId);
    await selectChat(userId);
    openChatBox(userId); // Pass the userId to openChatBox
  };

  if (loading) {
    return <div className="text-center mt-5">Loading...</div>;
  }

  if (error) {
    return <div className="text-center mt-5 text-danger">{error}</div>;
  }

  if (!user) {
    return <div className="text-center mt-5">User not found</div>;
  }

  const isOwnProfile = currentUserId === user.id;
  const ugles = user.userGameListEntries || [];

  return (
    <div className="container mt-4 px-0">
      <div className="mx-3">
        <ProfileInfo user={user} isOwn={isOwnProfile} />
        
        <ProfileActions
          isOwnProfile={isOwnProfile}
          isFollowing={isFollowing}
          onFollowToggle={handleFollowToggle}
          onMessageClick={handleMessageClick}
          onShowFollowing={() => setShowFollowingModal(true)}
          onShowFollowers={() => setShowFollowersModal(true)}
        />
        
        <GameListManager
          ugles={ugles}
          userName={user.displayName}
          isOwn={isOwnProfile}
        />
        
        <UserConnectionsModal
          show={showFollowingModal}
          onClose={() => setShowFollowingModal(false)}
          userId={userId!}
          type="following"
          isOwnProfile={isOwnProfile}
        />
        <UserConnectionsModal
          show={showFollowersModal}
          onClose={() => setShowFollowersModal(false)}
          userId={userId!}
          type="followers"
          isOwnProfile={isOwnProfile}
        />
      </div>
    </div>
  );
};

export default UserProfile;