package org.example;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlaskaSenateWebScraper {

    static class Senator {
        private String name;
        private String title;
        private String position;
        private String party;
        private String address;
        private String phone;
        private String email;
        private String url;

        public Senator(String name, String title, String position, String party,
                       String address, String phone, String email, String url) {
            this.name = name;
            this.title = title;
            this.position = position;
            this.party = party;
            this.address = address;
            this.phone = phone;
            this.email = email;
            this.url = url;
        }
    }

    public static void main(String[] args) {
        String url = "https://akleg.gov/senate.php";
        List<Senator> senators = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            System.out.println("Navigating to: " + url);
            page.navigate(url);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Get all senator containers - they are in column divs
            Locator containers = page.locator("div.col-md-6, div.col-lg-4, div.col-sm-6");

            // If that doesn't work, try a different approach
            if (containers.count() == 0) {
                containers = page.locator("a[href*='basis/Member/Detail']").locator("xpath=../..");
            }

            int count = containers.count();
            System.out.println("Found " + count + " senator containers");

            for (int i = 0; i < count; i++) {
                try {
                    Locator container = containers.nth(i);
                    String containerHtml = container.innerHTML();
                    String containerText = container.textContent();

                    // Extract profile URL
                    String profileUrl = "";
                    try {
                        Locator link = container.locator("a[href*='basis/Member/Detail']").first();
                        profileUrl = link.getAttribute("href");
                        if (profileUrl != null && !profileUrl.startsWith("http")) {
                            profileUrl = "http://www.akleg.gov" + profileUrl;
                        }
                    } catch (Exception e) {
                        continue; // Skip if no valid link
                    }

                    // Extract name - it's the link text, clean it
                    String name = "";
                    try {
                        Locator nameLink = container.locator("a[href*='basis/Member/Detail']").first();
                        name = nameLink.textContent().trim();
                        // Remove any extra whitespace or newlines
                        name = name.replaceAll("\\s+", " ").trim();
                        // Remove leadership titles that might be in the name
                        name = name.replaceAll("(Majority Leader|Minority Leader|Senate President|President)", "").trim();
                    } catch (Exception e) {
                        continue;
                    }

                    // Skip if name is too short or empty
                    if (name.length() < 3) continue;

                    // Extract data using regex patterns
                    String party = extractWithPattern(containerText, "Party:\\s*([A-Za-z]+)");
                    String district = extractWithPattern(containerText, "District:\\s*([A-Z])");
                    String city = extractWithPattern(containerText, "City:\\s*([^\\n]+?)(?=Party:|District:|Phone:|$)");
                    String phone = extractWithPattern(containerText, "Phone:\\s*([0-9\\-]+)");
                    String tollFree = extractWithPattern(containerText, "Toll-Free:\\s*([0-9\\-]+)");

                    // Build position from district
                    String position = "";
                    if (!district.isEmpty()) {
                        position = "District " + district;
                    }

                    // Check for leadership position in the text
                    if (containerText.contains("Majority Leader")) {
                        position = position.isEmpty() ? "Majority Leader" : position + " - Majority Leader";
                    } else if (containerText.contains("Minority Leader")) {
                        position = position.isEmpty() ? "Minority Leader" : position + " - Minority Leader";
                    } else if (containerText.contains("Senate President")) {
                        position = position.isEmpty() ? "Senate President" : position + " - Senate President";
                    }

                    // Build address
                    String address = city.trim();

                    // Build phone (prefer toll-free if available)
                    String phoneNumber = phone;
                    if (!tollFree.isEmpty() && !phone.isEmpty()) {
                        phoneNumber = phone + " / " + tollFree + " (Toll-Free)";
                    } else if (!tollFree.isEmpty()) {
                        phoneNumber = tollFree + " (Toll-Free)";
                    }

                    // Extract email (it's usually protected, but we can note it exists)
                    String email = "";
                    if (containerHtml.contains("email-protection") || containerText.toLowerCase().contains("email")) {
                        email = "Available via website";
                    }

                    String title = "Senator";

                    Senator senator = new Senator(name, title, position, party, address, phoneNumber, email, profileUrl);
                    senators.add(senator);

                    System.out.println("Scraped: " + name + " | " + party + " | " + position);

                } catch (Exception e) {
                    System.err.println("Error at index " + i + ": " + e.getMessage());
                }
            }

            browser.close();

        } catch (Exception e) {
            System.err.println("Error during web scraping: " + e.getMessage());
            e.printStackTrace();
        }

        // Remove duplicates based on name
        List<Senator> uniqueSenators = new ArrayList<>();
        List<String> seenNames = new ArrayList<>();
        for (Senator s : senators) {
            if (!seenNames.contains(s.name)) {
                uniqueSenators.add(s);
                seenNames.add(s.name);
            }
        }

        // Save to JSON file
        saveToJson(uniqueSenators, "alaska_senators.json");

        System.out.println("\n===========================================");
        System.out.println("Scraping completed!");
        System.out.println("Total senators scraped: " + uniqueSenators.size());
        System.out.println("Data saved to: alaska_senators.json");
        System.out.println("===========================================");
    }

    private static String extractWithPattern(String text, String patternStr) {
        try {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            // Pattern not found
        }
        return "";
    }

    private static void saveToJson(List<Senator> senators, String filename) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(senators, writer);
            System.out.println("\nJSON file created successfully: " + filename);
        } catch (IOException e) {
            System.err.println("Error writing to JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}