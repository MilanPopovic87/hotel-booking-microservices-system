import { Component } from '@angular/core';
import { UserService } from '../../core/services/user.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../core/services/chat.service';

interface Message {
  user: boolean; // true if user sent it
  text: string;
}

@Component({
  selector: 'app-chat',
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.html',
  styleUrls: ['./chat.scss'],
})
export class ChatComponent {
  inputMessage: string = '';

  constructor(
    private userService: UserService,
    private chatService: ChatService,
  ) {}

  get messages() {
    return this.chatService.getMessages();
  }

  sendMessage() {
    if (!this.inputMessage.trim()) return;

    const message = this.inputMessage;

    // Add user message
    this.chatService.addMessage({ user: true, text: message });
    this.inputMessage = '';

    // Create loading message reference
    const loadingMsg = { user: false, text: 'Thinking...' };
    this.chatService.addMessage(loadingMsg);

    const token = this.userService.getToken();
    if (!token) return;

    this.chatService.sendMessage(message, token).subscribe({
      next: (res) => {
        console.log('AI response:', res);
        // Replace ONLY this specific loading message
        const msgs = this.chatService.getMessages();
        const index = msgs.indexOf(loadingMsg);
        if (index !== -1) {
          this.messages[index] = { user: false, text: res.answer };
        }
      },
      error: (err) => {
        const msgs = this.chatService.getMessages();
        const index = msgs.indexOf(loadingMsg);

        if (index === -1) return;

        switch (err.status) {
          case 503:
            this.messages[index] = {
              user: false,
              text: 'AI service is not enabled.',
            };
            break;

          case 504:
            this.messages[index] = {
              user: false,
              text: 'AI response timed out.',
            };
            break;

          default:
            this.messages[index] = {
              user: false,
              text: 'AI service error.',
            };
        }
      },
    });
  }
}
