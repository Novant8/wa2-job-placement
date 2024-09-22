
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


## Workflow

1. A user can register in the application and choose a role: Customer or Professional.

<b>Customer Workflow:</b>

2. A customer can create a new job offer, specifying the duration and required skills.
3. After the job offer is created, the customer waits for an operator to select a suitable candidate. 
4. The customer has the option to either reject the candidate or submit a contract for hire the candidate .
5. If the professional accepts the contract, they can begin working for the customer. 
6. Once the customer believes the job is complete, they can conclude the job offer, making the professional available for other work.

<b>Professional Workflow:</b>
2. The professional receives proposals for job offers selected by the operator.
3. The professional waits for the customer to submit a valid contract.
4. The professional can either accept the terms of the contract or reject the proposal.
5. If the professional accepts, they can start working; otherwise, they remain available for other job offers.


## FAQ

**Can a professional be employed in more than one job at a time?**
- No, the design of the application allows a professional to work on only one job at a time.

**How can I stay updated on the events of a job offer?**
- The application will send an email notification for each new event related to a job offer associated with you.

**Who can see the statistics and KPIs?**
- Only managers have the privileges to view the statistics and KPIs.