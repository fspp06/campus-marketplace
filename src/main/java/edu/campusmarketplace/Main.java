package edu.campusmarketplace;

import edu.campusmarketplace.repository.InMemoryMarketplaceRepository;
import edu.campusmarketplace.repository.MarketplaceRepository;
import edu.campusmarketplace.repository.MySqlMarketplaceRepository;
import edu.campusmarketplace.service.MarketplaceService;
import edu.campusmarketplace.ui.MarketplaceFrame;
import javax.swing.SwingUtilities;

/** Starts the campus marketplace application with MySQL persistence or in-memory fallback. */
public final class Main {
    private Main() {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MarketplaceRepository repository;
            try {
                repository = new MySqlMarketplaceRepository();
                System.out.println(">>> Connected to MySQL Database: campus_marketplace (localhost:3306)");
            } catch (Exception ex) {
                System.out.println(">>> MySQL connection unavailable (" + ex.getMessage() + "). Running in in-memory mode.");
                repository = new InMemoryMarketplaceRepository();
            }

            MarketplaceService service = new MarketplaceService(repository);
            new MarketplaceFrame(service).setVisible(true);
        });
    }
}
