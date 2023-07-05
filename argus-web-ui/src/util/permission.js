export const isRead = (permission) => {
    return permission === "READ" ||
            permission === "READWRITE" ||
            permission === "ADMIN";
}

export const isWrite = (permission) => {
    return permission === "WRITE" ||
            permission === "READWRITE" ||
            permission === "ADMIN";
}

export const toRawPermission = (read, write, admin = false) => {
    if (admin) {
        return "ADMIN"
    } else if (read && write) {
        return "READWRITE"
    } else if (read) {
        return "READ"
    } else if (write) {
        return "WRITE"
    } else {
        return "ACCESS"
    }
};
