# Fair_Visitor
*Visitor Management System*  
Developed a Spring Boot-based Visitor Management System to streamline and automate visitor tracking and gate management processes. The application featured distinct user roles (Admin, Resident, Gatekeeper) with specific functionalities, such as:  
- *Admin:* Manage residents (add/update) via CSV uploads, update active/inactive status of residents.  
- *Resident:* View pending visitor requests and manage approval/rejection of visits.  
- *Gatekeeper:* Add visitor details, create a visit, update entry/exit times, and upload photos for visitor records.  

Key Technical Features:  
- *Database Management:* Used JPA for efficient CRUD operations and data persistence.  
- *Redis Integration:* Implemented caching for faster access to View pending Visits.  
- *Pagination and Sorting:* Provided seamless access to large datasets of View Resident Details.
- *Scheduled Tasks:* Automated marking visits as expired after 30 minutes.
- *Swagger Integration:* Enabled API documentation for seamless collaboration and testing.  
- *Spring Security:* Secured the application using Spring Basic Authentication.  

The project provided a robust solution for residential complexes to manage and monitor visitor activity effectively while maintaining security and operational efficiency.
