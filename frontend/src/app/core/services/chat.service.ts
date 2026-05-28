import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface Message {
  user: boolean;
  text: string;
}

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private messages: Message[] = [];

  constructor(private http: HttpClient) {}

  getMessages(): Message[] {
    return this.messages;
  }

  addMessage(msg: Message) {
    this.messages.push(msg);
  }

  clear() {
    this.messages = [];
  }

  sendMessage(message: string, token: string) {
    return this.http.post<{ response: string }>(
      `${environment.apiBaseUrl}/chat`,
      { message },
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );
  }
}
