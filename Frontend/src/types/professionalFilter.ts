export type UserSkill = string;

//export type EmploymentState = "UNEMPLOYED" | "EMPLOYED" | "NOT_AVAILABLE";
export interface ProfessionalFilter {
  location: string;
  skills: UserSkill[];
  employmentState: string | undefined;
}
