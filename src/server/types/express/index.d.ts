import { User } from "../../users/user";
import { Request } from 'express';

declare module "express-serve-static-core" {
   interface Request {
     user?: User;
   }
 }
 