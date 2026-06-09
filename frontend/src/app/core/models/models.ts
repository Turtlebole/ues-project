export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  username: string;
  role: string;
  profileImage?: string;
  reviews?: Review[];
  managedLocations?: Location[];
}

export interface Location {
  id: number;
  name: string;
  address: string;
  type: string;
  description: string;
  image: string;
  averageRating?: number;
  managers?: User[];
  upcomingEvents?: Event[];
}

export interface Event {
  id: number;
  name: string;
  locationId: number;
  locationName?: string;
  address: string;
  type: string;
  date: string;
  regular: boolean;
  free: boolean;
  price?: number;
  image?: string;
}

export interface Review {
  id: number;
  userFirstName: string;
  userLastName: string;
  eventName: string;
  locationId: number;
  comment?: string;
  rate?: Rate;
  hidden: boolean;
  createdAt: string;
  comments?: Comment[];
  eventOccurrenceCount?: number;
}

export interface Rate {
  performanceRating?: number;
  soundLightRating?: number;
  spaceRating?: number;
  overallRating?: number;
}

export interface Comment {
  id: number;
  userFirstName: string;
  userLastName: string;
  userRole: string;
  text: string;
  createdAt: string;
  replies?: Comment[];
}

export interface AccountRequest {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  username: string;
  requestDate: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

export interface Analytics {
  totalEvents: number;
  regularEvents: number;
  irregularEvents: number;
  freeEvents: number;
  paidEvents: number;
  topRatedLocations?: Location[];
  recentReviews?: Review[];
}
