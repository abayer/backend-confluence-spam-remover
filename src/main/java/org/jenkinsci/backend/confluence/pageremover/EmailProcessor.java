package org.jenkinsci.backend.confluence.pageremover;

import com.cybozu.labs.langdetect.Language;
import hudson.plugins.jira.soap.RemotePage;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Entry point for the app that responds to confluence notification emails
 *
 * @author Kohsuke Kawaguchi
 */
public class EmailProcessor {
    public static void main(String[] args) throws Exception {
        // we get email in stdin
        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(System.getProperties()), System.in);
        PageNotification n = PageNotification.parse(msg);
        if (n==null) {
            System.err.println("Not a confluence notification email: "+msg.getSubject());
            return;
        }

        System.err.println("Parsed "+n);

        if (n.action.equals("added")) {
            RemotePage p = new Connection().getPage("JENKINS", n.pageTitle);
            Language lang = new LanguageDetection().detect(p.getContent());

            String body = String.format("Language detection: %s\nWiki: %s\n\n\nSee https://github.com/jenkinsci/backend-confluence-spam-remover about this bot", lang, n);
            System.err.println(body);

            Message reply = msg.reply(false);
            reply.setFrom(new InternetAddress("spambot@infradna.com"));
            reply.setContent(body,"text/plain");
            Transport.send(reply);
        }
    }
}
