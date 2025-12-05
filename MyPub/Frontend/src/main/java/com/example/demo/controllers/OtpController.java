package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.AuthApi;
import com.example.demo.model.AuthResponse;
import com.example.demo.model.OtpVerifyRequest;
import com.example.demo.security.TempAuth;
import com.example.demo.security.TokenStore;
import com.example.demo.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class OtpController {
    @FXML private TextField code;

    @FXML
    public void initialize() {
        // Add Enter key support - pressing Enter in code field triggers verify
        code.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onVerify();
            }
        });
    }

    public void onVerify() {
        try {
            String c = code.getText() == null ? "" : code.getText().trim();
            if (c.isEmpty()) { AlertUtils.warn("Enter the OTP code."); return; }
            if (TempAuth.username == null || TempAuth.username.isBlank()) {
                AlertUtils.error("No username in memory. Please login again.");
                Launcher.go("login.fxml", "Login");
                return;
            }

            AuthResponse res = AuthApi.verifyOtp(new OtpVerifyRequest(TempAuth.username, c));

            // 🧠 Some backends return only a success/OK and expect client to call /me with token from body/data.
            // We require a token to navigate.
            String token = res.token();
            if (token == null || token.isBlank()) {
                AlertUtils.error("OTP verified, but no token was returned. Please try again.");
                return; // stay on OTP screen so user can retry/resend
            }

            TokenStore.set(token);
            if (TempAuth.username.equalsIgnoreCase("michaelh")) {
                Launcher.go("dashboard.fxml", "Orders Dashboard"); // removed the space
            } else {
                Launcher.go("orders.fxml", "Default Dashboard");
            }

        } catch (Exception ex) {
            AlertUtils.error("OTP verify failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void onResend() {
        try {
            if (TempAuth.username == null || TempAuth.username.isBlank()) { AlertUtils.warn("Login again to resend."); return; }
            AuthApi.requestOtp(TempAuth.username);
            AlertUtils.info("OTP resent.");
        } catch (Exception ex) {
            AlertUtils.error("Failed to resend: " + ex.getMessage());
        }
    }
    public void goLogin(){ Launcher.go("login.fxml", "Login"); }
}
