
## Table of Contents
- [SpringBootApplication](#SpringBootApplication)
- [Functional Requirements](#functional-requirements) 
- [NonFunctional Requirements](#nonfunctionalrequirements)
- [Bonus Points](#Bonus-Points)
- 
- 
- 
- 
## SpringBootApplication

Introducing SynchronyImgurRESTApp, a REST Application for allow users to register, authenticate, and manage images. 
This project is designed and developed in order to integrate with Imgur API to upload, view, and delete images while associating them with the user profile.

## Functional Requirements
-  Register users with basic information, including a username and password.
-  After user authentication, allow image upload, viewing, and deletion.
-  Each user can have an associated list of images.
-  User authentication will be managed using a username and password stored in an in-memory H2 database.
-  The application should log important events and errors.
-  JUnit test cases must be implemented.

## NonFunctional Requirements
-  Spring Boot 3.x.x framework
-  JDK 17 or lower
-  H2 Database (in-memory for storing user information)
-  Imgur API Integration for image operations
-  Security
-  Scalable

## Bonus Points
-  Secure API via oAuth2
-  Optimize API for 100K RPM
-  Establish CI/CD Pipeline (feel free to use open-source tools)
-  Create a messaging event that publishes the username and the image name to a Messaging Platform (Ex: Kafka)
    & feel free to connect with a local or cloud instance of the Messaging Platform.
-  Preferably following the TDD approach for Junit test cases

## Tech Stack

**Client:**

- ![React](https://img.shields.io/badge/react-%2320232a.svg?style=for-the-badge&logo=react&logoColor=%2361DAFB)
- ![React Router](https://img.shields.io/badge/React_Router-CA4245?style=for-the-badge&logo=react-router&logoColor=white)
- ![Redux](https://img.shields.io/badge/redux-%23593d88.svg?style=for-the-badge&logo=redux&logoColor=white)
- ![Vite](https://img.shields.io/badge/vite-%23646CFF.svg?style=for-the-badge&logo=vite&logoColor=white)
- ![SASS](https://img.shields.io/badge/SASS-hotpink.svg?style=for-the-badge&logo=SASS&logoColor=white)


**Server:** 

- ![Socket.io](https://img.shields.io/badge/Socket.io-black?style=for-the-badge&logo=socket.io&badgeColor=010101)
- ![Express.js](https://img.shields.io/badge/express.js-%23404d59.svg?style=for-the-badge&logo=express&logoColor=%2361DAFB)
- ![Nodemon](https://img.shields.io/badge/NODEMON-%23323330.svg?style=for-the-badge&logo=nodemon&logoColor=%BBDEAD)
- ![NodeJS](https://img.shields.io/badge/node.js-6DA55F?style=for-the-badge&logo=node.js&logoColor=white)

**Database:**

- ![MongoDB](https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white)

**Other Software Packages:**

- ![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
- ![Visual Studio Code](https://img.shields.io/badge/Visual%20Studio%20Code-0078d7.svg?style=for-the-badge&logo=visual-studio-code&logoColor=white)




## Dependencies

This section includes all the Node Dependencies that are used in the project.

To install the Dependencieson server side, use:
```
    cd api
    npm install requirement_server.txt
```

To install the Dependencieson client side, use:
```
    cd api
    npm install requirement_client.txt
```

So the Dependencies in Server side are:

```
    dependencies:
        "@prisma/client": "^5.17.0",
        "cookie-parser": "^1.4.6",
        "express": "^4.19.2",
        "nodemon": "^3.1.4",
        "socket.io": "^4.7.5"



        .....and many more.....

```

And the Dependencies in Client side are:

```
    dependencies:
        "@reduxjs/toolkit": "^2.2.7",
        "axios": "^1.7.2",
        "dompurify": "^3.1.6",
        "leaflet": "^1.9.4",
        "react": "^18.2.0",
        "react-dom": "^18.2.0",
        "react-leaflet": "^4.2.1",
        "react-redux": "^9.1.2",
        "react-router-dom": "^6.22.2",
        "react-toastify": "^10.0.5",
        "sass": "^1.71.1",
        "socket.io-client": "^4.7.5",
        "timeago.js": "^4.0.2",
        "zustand": "^4.5.4",
        "react-share":


    .....and many more.....

```

## Features
- User Can Register as a Customer or Agent. Further, they can view their dashboard and can update their profile details.
- Anyone Can view the Post(RealEstate Property). Users can search the Post on the basis of various parameters such as City, Numer Of Rooms etc.
- Agents Can Add, Delete, and Update the Property.
- Customer and Agent Can have a real time chat. In fact, they can view past messages from Other users.
- Customer can subscribe to their favorite Agents to receive notification whenever they add a Property.
- Customers and Agents can like, comment, and share the Post on Social Media.
- Customer can submit Property and Agent Reviews.
- Admin Can Track The activities of Agent such as CRUD Operations done by Agents.
- Agent can register(create account) through verification from Admin.


## Usage
The RealEstate App streamlines property search and management for users, agents, and administrators. Property seekers can explore listings using advanced filters, save favorites, and track their search history for quick access. Notifications keep users updated on price drops or new listings matching their criteria, while a real-time chat system enables seamless communication with agents. Social features like likes, comments, and sharing enhance user engagement, while administrators can manage agent verifications and oversee platform activities through a dedicated dashboard. This comprehensive solution simplifies the property discovery process and improves user experience.

## API Flow and Their Usage
This section will describe the usage of each API that is used in this project.

1. Authentication and Authorization APIs
    - `POST /api/auth/register` -> Registration
    - `POST /api/auth/login` -> Login
    - `POST /api/auth/logout` -> LogOut

2. Post Flow APIs
    - `GET /api/post` -> Get All Posts
    - `GET /api/post/{postId}` -> Get Single Post
    - `POST /api/post` -> Add a Post
    - `PUT /api/post/{postId}` -> Update a Post
    - `DELETE /api/post/{postId}` -> Delete a Post
    - `POST /api/post/like` -> Like a Post
    - `POST /api/post/comment` -> Comment a Post
    - `POST /api/post/rating` -> Rate a Post

3. Chat Flow APIs
    - `GET /api/chat` -> Get All Chats
    - `GET /api/chat/chatId` -> Get a Particular Chat
    - `POST /api/chat` -> Create a new Chat
    - `DELETE /api/chat/{chatId}` -> Delete a Chat
    - `PUT /api/chat/read/{chatId}` -> Read a Chat
 
4. Message Flow APIs
    - `POST /api/message/{chatId}` -> Add a message to Particular Chat
    - `PUT /api/message/{chatId}` -> Read a message of a Particular Chat

5. User Flow APIs
    - `GET /api/user/agents` -> Get All Agents
    - `PUT /api/user/{userId}` -> Update a User
    - `DELETE /api/user/{userId}` -> Delete a User
    - `POST /api/user/save` -> Save the Post
    - `GET /api/user/profilePosts` -> Get all the Saved Posts
    - `GET /api/user/notification` -> Get Number of Notifications
    - `POST /api/user/subscribe` -> User can Subscribe another user
    - `POST /api/user/add/notification` -> Add a notification
    - `POST /api/user/favorite/city` -> User can mark or unmark a city as favorite.
    - `GET /api/user/fetch/notification` -> Get All Notifications related to User
    - `GET /api/user/fetch/favorite/cities` -> Get Favorite Cities
    - `PUT /api/user/read/notification/:notificationId` -> Read a Notification



