import {Injectable} from "@angular/core";
import {BehaviorSubject, Observable} from "rxjs";
import {Notification} from "../model/Notification";
import {Client} from "@stomp/stompjs";
import taste_api from "../api/taste_api";
import {NotificationToastService} from "./notification.toast.service";

/**
 * At this moment, the NotificationsService is not ready to be used in the app.
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationsService{
  private notifications = new BehaviorSubject<Notification[]>([])
  private unreadCount = new BehaviorSubject<number>(0);
  private stompClient: Client;
  private sessionToken: string;

  constructor(
    private notificationToastService: NotificationToastService
  ) {
    this.sessionToken = this.getSessionToken();
    this.stompClient = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      connectHeaders: {
        Authorization: `${this.sessionToken}`
      }
    })
    if(this.sessionToken === ''){
      console.error('User is not logged in, cannot connect to WebSocket');
      return
    }
    this.initializeWebSocketConnection();
  }
  getNotifications(): Observable<Notification[]> {
    return this.notifications.asObservable();
  }

  getUnreadCount(): Observable<number> {
    return this.unreadCount.asObservable();
  }
  getSessionToken(): string{
    const sessionToken = localStorage.getItem('sessionToken');
    return sessionToken ? sessionToken : '';
  }
  private initializeWebSocketConnection(): void {
    this.stompClient.onConnect = async () => {
      console.log('Connected to TasteIT WebSocket!');
      this.stompClient.subscribe('/user/topic/notification', message => {
        const notification: Notification = JSON.parse(message.body);

        const currentNotifications = this.notifications.value;
        this.notifications.next([notification, ...currentNotifications]);

        this.unreadCount.next(this.unreadCount.value + 1);
        console.log('Received notification:', notification);
        this.notificationToastService.show(notification);
      });

      this.stompClient.subscribe('/user/topic/notification/read', message => {
        const notificationId = message.body;
        const currentNotifications = this.notifications.value;

        const updatedNotifications = currentNotifications.map(notif =>
          notif.notificationId === notificationId
            ? { ...notif, read: true }
            : notif
        );

        this.notifications.next(updatedNotifications);
      });
      await this.fetchInitialNotifications();
    };

    this.stompClient.activate();
  }
  private async fetchInitialNotifications(): Promise<void> {
    try {
      const notificationsRes = await taste_api.get('notification', {
        params: {
          page: 0,
          size: 10
        }
      });
      console.log('Received notifications response:', notificationsRes);

      const notifications = notificationsRes.data?.content || [];
      this.notifications.next(notifications as Notification[]);

      const countRes = await taste_api.get('notification/unread/count');
      const count = typeof countRes.data === 'number' ? countRes.data : 0;
      this.unreadCount.next(count);
    } catch (error: any) {
      console.error('Error fetching notifications:', error.response?.data || error);
      this.notifications.next([]);
      this.unreadCount.next(0);
      return Promise.reject(error.response?.data || error);
    }
  }
  async markAsRead(notificationId: string): Promise<void> {
    try {
      await taste_api.put(`notification/${notificationId}/read`);

      this.unreadCount.next(this.unreadCount.value - 1);

      const updatedNotifications = this.notifications.value.map(notif =>
        notif.notificationId === notificationId
          ? { ...notif, read: true }
          : notif
      );
      this.notifications.next(updatedNotifications);
    } catch (error: any) {
      console.error('Error marking notification as read:', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

}
