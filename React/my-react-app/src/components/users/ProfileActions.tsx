

import React from 'react';
import { Button } from 'react-bootstrap';

interface Props {
  isOwnProfile: boolean;
  isFollowing?: boolean;
  onFollowToggle?: () => void;
  onMessageClick?: () => void;
  onShowFollowing: () => void;
  onShowFollowers: () => void;
}

const ProfileActions: React.FC<Props> = ({
  isOwnProfile,
  isFollowing = false,
  onFollowToggle,
  onMessageClick,
  onShowFollowing,
  onShowFollowers,
}) => {
  return (
    <div className="mb-3 d-flex justify-content-between gap-2">
      <div className="d-flex gap-2">
        <Button variant="outline-dark" size="lg" onClick={onShowFollowing}>
          Following
        </Button>
        <Button variant="outline-dark" size="lg" onClick={onShowFollowers}>
          Followers
        </Button>
      </div>
      
      {!isOwnProfile && (
        <div className="d-flex gap-2">
          <Button 
            variant="outline-primary" 
            size="lg" 
            onClick={onMessageClick}
          >
            Message
          </Button>
          <Button 
            variant={isFollowing ? "outline-danger" : "outline-dark"} 
            size="lg" 
            onClick={onFollowToggle}
          >
            {isFollowing ? "Unfollow" : "Follow"}
          </Button>
        </div>
      )}
    </div>
  );
};

export default ProfileActions;