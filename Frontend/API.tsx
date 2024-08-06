const url :string = "http://localhost:8080"
interface JobOfferCreateDTO {
    description: string;
    requiredSkills: string[];
    duration: number;
    notes: string |null;
}
async function addJobOffer(job: JobOfferCreateDTO, token: string | undefined):Promise<number> {
    return new Promise((resolve, reject)=>{
        //TODO: change fixed customer id
        fetch(url+ '/crm/API/customers/1/job-offers',{
            method: 'POST',

            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': `${token}`
            },
            body: JSON.stringify(job),
        }).then((response)=>{
            if (response.ok){
                response.json()
                    .then((id)=>resolve(id))
                    .catch(() => { reject({ error: "Cannot parse server response." }) });
            }else{
                // analyze the cause of error
                response.json()
                    .then((message) => { reject(message); }) // error message in the response body
                    .catch(() => { reject({ error: "Cannot parse server response." }) });
            }
        }).catch(() => { reject({ error: "Cannot communicate with the server." }) })
    })
}


async function getProfessionals(token: string | undefined):Promise<any> {
    return new Promise((resolve, reject)=>{
        fetch(url+ '/crm/API/professionals',{
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': `${token}`
            },
        }).then((response)=>{
            if (response.ok){
                response.json()
                    .then((prof)=>resolve(prof))
                    .catch(() => { reject({ error: "Cannot parse server response." }) });
            }else{
                response.json()
                    .then((message) => { reject(message); })
                    .catch(() => { reject({ error: "Cannot parse server response." }) });
            }
        }).catch(() => { reject({ error: "Cannot communicate with the server." }) })
    })
}




const API = {
    addJobOffer,
    getProfessionals
};
export default API;