import { defineStore } from "pinia";
import { Nullable } from "@/lib/utils/null";
import { Message } from "@/lib/structures/Messages";
import { User } from "@/lib/structures/Users";
function useChannelStore<Id extends string>(id: Id) {
defineStore(`Channel-${id}`, {
    state: () => ({
        name: null as Nullable<String>,
        id: '' as string,
        messages: new Map<string, Message>(),
        settings: {
            allowNSFW: false,
            allowNotifications: true,
            showTyping: true,
            showActive: false
        } as Nullable<ChannelSettings>,
        usersActive: new Array<User>(),
        typingUsers: new Map<string, User>()
    }),
    actions: {
        
    }
})
}




export interface ChannelSettings {
    allowNSFW: boolean;
    allowNotifications: boolean;
    showTyping: boolean;
    showActive: boolean;
}