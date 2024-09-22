
## How to Run

### Option 1: Using Docker Compose [Suggested]
To run the application using pre-built images of each microservices from Docker Hub:

1. Clone the repository
   ```bash
   git clone https://github.com/Novant8/wa2-job-placement
1. Navigate to the project's root directory.
2. Run the following command:
   ```bash
   docker compose up
3. If you made changes in to the code and want to rebuild the images, use:
   ```bash
    docker compose up --build
The Docker Compose file ensures that all microservices are correctly linked and configured to communicate with each other.

### Option 2: Manually
You can manually start each microservice by navigating to the root directory of each service.

## Microservices

| **Service**               | **URL Path**                                      | **Description**                                                                                   |
|---------------------------|--------------------------------------------------|---------------------------------------------------------------------------------------------------|
| **Frontend**              | `http://localhost:8080`                          | Hosts the React-based frontend of the application.                                                |
| **Document Store**        | `http://localhost:8080/document-store/API/...`   | Manages documents uploaded to the application, such as CVs.                                       |
| **CRM**                   | `http://localhost:8080/CRM/API/...`              | Contains the application's business logic and customer relationship management functionalities.    |
| **Communication Manager** | `http://localhost:8080/communication-manager/API/` | Handles sending and receiving emails, connected through Gmail.                                    |
| **Monitoring**            | `http://localhost:3000/...`                      | Collects statistics on microservices and tracks key performance indicators (KPIs).                |


## Users

| **Username**         | **Password**         | **Role**        |
|----------------------|----------------------|-----------------|
| mario.rossi          | mario.rossi.g07       | Manager         |
| luigi.verdi          | luigi.verdi.g07       | Operator        |
| luigi.verdi          | luigi.verdi.g07       | Operator        |
| company.a            | company.a.g07         | Customer        |
| company.b            | company.b.g07         | Customer        |
| luca.rossi           | luca.rossi.g07        | Professional    |
| giovanni.mariani     | giovanni.mariani.g07  | Professional    |
