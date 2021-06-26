import { User } from "../../users/user";
import { Request } from 'express';
import { Budget } from "../../budget/model";

declare module "express-serve-static-core" {
   interface Request {
     user?: User;
     budget?: Budget;
   }
 }
 