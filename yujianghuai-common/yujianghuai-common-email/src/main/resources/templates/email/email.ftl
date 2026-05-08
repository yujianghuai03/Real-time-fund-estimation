<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ConnectSphere 邮件通知</title>
    <style>
        /* Reset styles for email clients */
        body, table, td, a {
            -webkit-text-size-adjust: 100%;
            -ms-text-size-adjust: 100%;
            margin: 0;
            padding: 0;
            border: 0;
            font-size: 100%;
            font: inherit;
            vertical-align: baseline;
        }

        table {
            border-collapse: collapse;
            mso-table-lspace: 0pt;
            mso-table-rspace: 0pt;
        }

        img {
            border: 0;
            height: auto;
            line-height: 100%;
            outline: none;
            text-decoration: none;
            -ms-interpolation-mode: bicubic;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
            padding: 20px;
            font-family: Arial, sans-serif;
            color: #333333;
        }

        .header {
            font-size: 24px;
            font-weight: bold;
            color: #2d3748;
            padding-bottom: 10px;
            border-bottom: 2px solid #e2e8f0;
        }

        .info {
            margin: 15px 0;
        }

        .label {
            font-weight: bold;
            color: #4a5568;
        }

        .content-box {
            margin-top: 10px;
            padding: 15px;
            background-color: #f1f5f9;
            border-left: 4px solid #4f46e5;
            border-radius: 4px;
        }

        .footer {
            margin-top: 20px;
            font-size: 14px;
            color: #718096;
        }

        a {
            color: #3182ce;
            text-decoration: underline;
        }

        .signature {
            margin-top: 20px;
            font-style: italic;
            color: #4a5568;
        }

        .security-warning {
            margin-top: 20px;
            padding: 15px;
            background-color: #fef9c3;
            border-left: 4px solid #facc15;
            border-radius: 4px;
            font-size: 14px;
            color: #854d0e;
        }

        @media only screen and (max-width: 600px) {
            .container {
                width: 100% !important;
                padding: 15px;
            }
        }
    </style>
</head>
<body style="margin:0; padding:0; background-color:#f7f9fc;">
<center>
    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color:#f7f9fc;">
        <tr>
            <td align="center" valign="top">
                <table class="container" align="center" border="0" cellpadding="0" cellspacing="0" width="600" style="background-color:#ffffff; border-radius:8px; box-shadow:0 2px 8px rgba(0,0,0,0.05); font-family:Arial,sans-serif; color:#333333; padding:20px;">
                    <!-- Header -->
                    <tr>
                        <td class="header">尊敬的用户：</td>
                    </tr>

                    <!-- Body -->
                    <tr>
                        <td style="padding-top:15px;">
                            <p>您好！</p>
                            <p>感谢您使用 <strong>ConnectSphere</strong>。我们收到了您的操作请求，以下是相关信息说明：</p>

                            <div class="info"><span class="label">【邮件类型】：</span> ${type}</div>
                            <div class="info"><span class="label">【操作时间】：</span> ${time}</div>
                            <div class="info"><span class="label">【关联账号】：</span> ${email}</div>
                            <div class="content-box">
                                【操作内容】：${content}
                            </div>

                            <p class="footer">
                                如该操作非您本人所为，请立即通过以下方式联系我们：<br>
                                📞 客服邮箱：<a href="mailto:support@connectsphere.com">support@connectsphere.com</a><br>
                                🌐 官方网站：<a href="https://www.connectsphere.com " target="_blank">https://www.connectsphere.com </a>
                            </p>

                            <div class="security-warning">
                                ⚠️ 温馨提示：为了保障账户安全，请勿将此邮件内容透露给他人。建议定期修改密码，并开启双重验证功能。
                            </div>

                            <p class="signature">
                                祝您使用愉快！<br>
                                此致<br>
                                ConnectSphere 团队<br>
                                ${year} 年
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</center>
</body>
</html>