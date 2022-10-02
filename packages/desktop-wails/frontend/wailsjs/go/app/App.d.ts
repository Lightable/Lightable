// Cynhyrchwyd y ffeil hon yn awtomatig. PEIDIWCH Â MODIWL
// This file is automatically generated. DO NOT EDIT
import {mocks} from '../models';
import {app} from '../models';

export function ChangeTheme(arg1:string):void;

export function CreateResponder():void;

export function DownloadUpdate(arg1:string):Promise<string>;

export function ForceGC():void;

export function GetColour():Promise<string>;

export function GetConfig():Promise<mocks.AppConfig>;

export function GetMemoryStats():Promise<app.CustomMemoryStats>;

export function GetRunningGoRoutines():Promise<number>;

export function GetVersion():Promise<string>;

export function HasUser(arg1:boolean):void;

export function OnRouteChange(arg1:string,arg2:string):void;

export function PingDelay():void;

export function PreInit():void;

export function Restart():void;

export function SaveConfig():void;

export function SelectSideDrawerPhoto():Promise<any>;

export function SetCurrentUser(arg1:mocks.PrivateUser):void;

export function SetUser(arg1:string,arg2:mocks.PrivateUser):void;
