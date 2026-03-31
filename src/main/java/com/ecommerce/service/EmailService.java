package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${tiswa.admin-email}")
    private String adminEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    private String getHeader() {
        return "<div style=\"font-family: 'Georgia', serif; background-color: #0a0a0a; color: #c9a962; padding: 20px; text-align: center;\">" +
                "<h1 style=\"margin: 0; font-size: 28px; letter-spacing: 2px;\">TISWA</h1>" +
                "<p style=\"margin: 5px 0 0; font-family: 'Arial', sans-serif; font-size: 12px; color: #8b8b8b; text-transform: uppercase; letter-spacing: 1px;\">Curated for the Extraordinary</p>" +
                "</div>" +
                "<div style=\"font-family: 'Arial', sans-serif; padding: 30px; background-color: #faf8f5; color: #2d2d2d; line-height: 1.6;\">";
    }

    private String getFooter() {
        return "</div>" +
                "<div style=\"font-family: 'Arial', sans-serif; background-color: #0a0a0a; color: #8b8b8b; padding: 20px; text-align: center; font-size: 12px;\">" +
                "<p>© " + java.time.Year.now() + " TISWA. All rights reserved.</p>" +
                "</div>";
    }

    @Async
    public void sendWelcomeEmail(User user) {
        String roleText = user.getRole().getName().equals("SELLER") ? "seller" : "member";
        String subject = "Welcome to TISWA";
        String body = getHeader() +
                "<h2 style=\"color: #1a1a1a; margin-top: 0;\">Welcome, " + user.getName() + "</h2>" +
                "<p>Thank you for joining TISWA. We are thrilled to have you as a new " + roleText + ".</p>" +
                "<p>Discover what defines you.</p>" +
                getFooter();
        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendOrderConfirmation(User buyer, Order order) {
        String subject = "Order Confirmation - #" + order.getId();
        StringBuilder body = new StringBuilder(getHeader());
        body.append("<h2 style=\"color: #1a1a1a; margin-top: 0;\">Thank you for your order!</h2>");
        body.append("<p>Hi ").append(buyer.getName()).append(",</p>");
        body.append("<p>We've received your order <strong>#").append(order.getId()).append("</strong> and are getting it ready.</p>");
        
        body.append("<table style=\"width: 100%; border-collapse: collapse; margin-top: 20px;\">");
        body.append("<tr><th style=\"text-align: left; padding: 10px; border-bottom: 2px solid #e2e8f0;\">Item</th>")
            .append("<th style=\"text-align: right; padding: 10px; border-bottom: 2px solid #e2e8f0;\">Price</th></tr>");
        
        for (OrderItem item : order.getItems()) {
            body.append("<tr><td style=\"padding: 10px; border-bottom: 1px solid #e2e8f0;\">")
                .append(item.getProduct().getName()).append(" x").append(item.getQuantity())
                .append("</td><td style=\"text-align: right; padding: 10px; border-bottom: 1px solid #e2e8f0;\">₹")
                .append(item.getPriceAtPurchase().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .append("</td></tr>");
        }
        
        body.append("<tr><th style=\"text-align: left; padding: 10px;\">Total</th>")
            .append("<th style=\"text-align: right; padding: 10px; color: #c9a962;\">₹").append(order.getTotalAmount()).append("</th></tr>");
        body.append("</table>");
        
        if (order.getEstimatedDelivery() != null) {
            body.append("<p style=\"margin-top: 20px;\"><strong>Estimated Delivery:</strong> ").append(order.getEstimatedDelivery()).append("</p>");
        }
        
        body.append(getFooter());
        sendHtmlEmail(buyer.getEmail(), subject, body.toString());
    }

    @Async
    public void sendOrderStatusUpdate(User buyer, Order order, String oldStatus, String newStatus) {
        String subject = "Order Update - #" + order.getId() + " is now " + newStatus;
        String body = getHeader() +
                "<h2 style=\"color: #1a1a1a; margin-top: 0;\">Order Update</h2>" +
                "<p>Hi " + buyer.getName() + ",</p>" +
                "<p>The status of your order <strong>#" + order.getId() + "</strong> has changed from <strong>" + oldStatus + "</strong> to <strong>" + newStatus + "</strong>.</p>" +
                getFooter();
        sendHtmlEmail(buyer.getEmail(), subject, body);
    }

    @Async
    public void sendOrderCancellation(User buyer, Order order) {
        String subject = "Order Cancelled - #" + order.getId();
        String body = getHeader() +
                "<h2 style=\"color: #1a1a1a; margin-top: 0;\">Order Cancelled</h2>" +
                "<p>Hi " + buyer.getName() + ",</p>" +
                "<p>Your order <strong>#" + order.getId() + "</strong> has been successfully cancelled. If you had already been charged, a refund will be processed shortly.</p>" +
                getFooter();
        sendHtmlEmail(buyer.getEmail(), subject, body);
    }

    @Async
    public void sendNewOrderNotification(User seller, Order order, List<OrderItem> items) {
        String subject = "New Order Received - TISWA";
        StringBuilder body = new StringBuilder(getHeader());
        body.append("<h2 style=\"color: #1a1a1a; margin-top: 0;\">Good news, ").append(seller.getName()).append("!</h2>");
        body.append("<p>You have received a new order. Please prepare the following items for shipment:</p>");
        
        body.append("<ul style=\"padding-left: 20px;\">");
        for (OrderItem item : items) {
            body.append("<li><strong>").append(item.getProduct().getName()).append("</strong> (Quantity: ").append(item.getQuantity()).append(")</li>");
        }
        body.append("</ul>");
        
        body.append("<p>Order ID for reference: <strong>#").append(order.getId()).append("</strong></p>");
        body.append(getFooter());
        sendHtmlEmail(seller.getEmail(), subject, body.toString());
    }

    @Async
    public void sendAdminNotification(String eventSubject, String details) {
        String body = getHeader() +
                "<h2 style=\"color: #1a1a1a; margin-top: 0;\">Admin Alert: " + eventSubject + "</h2>" +
                "<div style=\"background-color: #fff; padding: 15px; border-left: 4px solid #c9a962; margin-bottom: 20px;\">" +
                "<p style=\"margin: 0;\">" + details + "</p>" +
                "</div>" +
                getFooter();
        sendHtmlEmail(adminEmail, "TISWA Admin Alert: " + eventSubject, body);
    }

    @Async
    public void sendLowStockAlert(User seller, Product product) {
        String subject = "Low Stock Alert: " + product.getName();
        String body = getHeader() +
                "<h2 style=\"color: #1a1a1a; margin-top: 0;\">Low Stock Alert</h2>" +
                "<p>Hi " + seller.getName() + ",</p>" +
                "<p>Your product <strong>" + product.getName() + "</strong> is running low on stock. There are only <strong>" + product.getStock() + "</strong> left.</p>" +
                "<p>Please restock soon to continue selling.</p>" +
                getFooter();
        sendHtmlEmail(seller.getEmail(), subject, body);
    }
}
