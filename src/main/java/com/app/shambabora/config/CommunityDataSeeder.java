package com.app.shambabora.config;

import com.app.shambabora.entity.Role;
import com.app.shambabora.entity.User;
import com.app.shambabora.modules.collaboration.entity.Group;
import com.app.shambabora.modules.collaboration.entity.GroupMembership;
import com.app.shambabora.modules.collaboration.entity.Post;
import com.app.shambabora.modules.collaboration.repository.GroupRepository;
import com.app.shambabora.modules.collaboration.repository.GroupMembershipRepository;
import com.app.shambabora.modules.collaboration.repository.PostRepository;
import com.app.shambabora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after DataSeeder
public class CommunityDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedSampleUsers();
        seedSampleGroups();
        seedSamplePosts();
    }

    private void seedSampleUsers() {
        List<String[]> sampleUsers = List.of(
                new String[]{"john_farmer", "john@example.com", "John Doe", "+254700000010"},
                new String[]{"mary_grower", "mary@example.com", "Mary Wanjiku", "+254700000011"},
                new String[]{"peter_maize", "peter@example.com", "Peter Kamau", "+254700000012"},
                new String[]{"grace_crops", "grace@example.com", "Grace Muthoni", "+254700000013"}
        );

        for (String[] userData : sampleUsers) {
            if (userRepository.findByUsername(userData[0]).isEmpty()) {
                try {
                    User user = User.builder()
                            .username(userData[0])
                            .email(userData[1])
                            .password(passwordEncoder.encode("password123"))
                            .fullName(userData[2])
                            .phoneNumber(userData[3])
                            .roles(Set.of(Role.FARMER))
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    userRepository.save(user);
                    log.info("✅ Created sample user: {}", userData[0]);
                } catch (Exception e) {
                    log.error("❌ Error creating sample user {}: ", userData[0], e);
                }
            }
        }
    }

    private void seedSampleGroups() {
        if (groupRepository.count() > 0) {
            log.info("Groups already exist, skipping group creation");
            return;
        }

        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            log.warn("Admin user not found, skipping group creation");
            return;
        }

        List<String[]> sampleGroups = List.of(
                new String[]{"Maize Farmers Kenya", "A community for maize farmers across Kenya to share experiences and best practices"},
                new String[]{"Organic Farming Group", "Dedicated to promoting organic farming methods and sustainable agriculture"},
                new String[]{"Nyeri Farmers Association", "Local farmers group for Nyeri county members"},
                new String[]{"Market Updates & Prices", "Stay updated with latest market prices and trading opportunities"}
        );

        for (String[] groupData : sampleGroups) {
            try {
                Group group = Group.builder()
                        .name(groupData[0])
                        .description(groupData[1])
                        .ownerId(admin.getId())
                        .createdAt(Instant.now())
                        .build();

                Group savedGroup = groupRepository.save(group);

                // Add admin as a member
                GroupMembership membership = GroupMembership.builder()
                        .groupId(savedGroup.getId())
                        .userId(admin.getId())
                        .role(GroupMembership.MembershipRole.OWNER)
                        .status(GroupMembership.MembershipStatus.ACTIVE)
                        .joinedAt(Instant.now())
                        .build();

                groupMembershipRepository.save(membership);
                log.info("✅ Created sample group: {}", groupData[0]);
            } catch (Exception e) {
                log.error("❌ Error creating sample group {}: ", groupData[0], e);
            }
        }
    }

    private void seedSamplePosts() {
        if (postRepository.count() > 0) {
            log.info("Posts already exist, skipping post creation");
            return;
        }

        List<User> users = userRepository.findAllActive();
        if (users.isEmpty()) {
            log.warn("No users found, skipping post creation");
            return;
        }

        List<String[]> samplePosts = List.of(
                new String[]{"Welcome to ShambaBora Community! 🌾", "ANNOUNCEMENT", "Hello fellow farmers! Welcome to our new community platform. Here we can share knowledge, ask questions, and support each other in our farming journey. Let's grow together!"},
                new String[]{"Best time to plant maize in Central Kenya?", "QUESTION", "I'm planning to plant maize this season. What's the best time to plant in Central Kenya region? Any advice on the best varieties for this area?"},
                new String[]{"Great harvest this season! 🌽", "SHARE_EXPERIENCE", "Just finished harvesting my maize crop and I'm happy to report a 30% increase compared to last season. The key was proper soil testing and using the right fertilizer. Happy to share more details if anyone is interested!"},
                new String[]{"Dealing with Fall Armyworm", "ADVICE", "For those struggling with fall armyworm, I've found that early detection and using biological control methods work well. Check your crops regularly, especially young plants. Neem-based pesticides have been effective for me."},
                new String[]{"Market prices update - Maize", "MARKET_UPDATE", "Current maize prices at Muthurwa Market: KSH 45 per kg for grade 1, KSH 40 per kg for grade 2. Prices have been stable this week. Good time to sell if you have stock."},
                new String[]{"Heavy rains expected this week ⛈️", "WEATHER_ALERT", "Kenya Meteorological Department has issued a heavy rainfall warning for this week. Farmers are advised to ensure proper drainage in their fields and protect young crops. Stay safe everyone!"}
        );

        for (int i = 0; i < samplePosts.size(); i++) {
            try {
                String[] postData = samplePosts.get(i);
                User author = users.get(i % users.size());

                Post post = Post.builder()
                        .authorId(author.getId())
                        .content(postData[2])
                        .postType(Post.PostType.valueOf(postData[1]))
                        .status(Post.PostStatus.ACTIVE)
                        .createdAt(Instant.now().minusSeconds(3600 * (samplePosts.size() - i))) // Stagger creation times
                        .updatedAt(Instant.now().minusSeconds(3600 * (samplePosts.size() - i)))
                        .build();

                postRepository.save(post);
                log.info("✅ Created sample post: {}", postData[0]);
            } catch (Exception e) {
                log.error("❌ Error creating sample post: ", e);
            }
        }
    }
}
