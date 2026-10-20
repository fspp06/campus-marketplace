package edu.campusmarketplace;

import edu.campusmarketplace.repository.InMemoryMarketplaceRepository;
import edu.campusmarketplace.service.MarketplaceService;
import edu.campusmarketplace.ui.MarketplaceFrame;
import javax.swing.SwingUtilities;

/** Starts one isolated, in-memory campus marketplace session. */
public final class Main {
    private Main() {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MarketplaceService service = new MarketplaceService(new InMemoryMarketplaceRepository());
            new MarketplaceFrame(service).setVisible(true);
        });
    }
}
