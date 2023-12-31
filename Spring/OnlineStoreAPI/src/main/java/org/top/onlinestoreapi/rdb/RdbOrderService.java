package org.top.onlinestoreapi.rdb;

import org.springframework.stereotype.Service;
import org.top.onlinestoreapi.entity.Client;
import org.top.onlinestoreapi.entity.Item;
import org.top.onlinestoreapi.entity.Order;
import org.top.onlinestoreapi.entity.Position;
import org.top.onlinestoreapi.rdb.repository.OrderRepository;
import org.top.onlinestoreapi.rdb.repository.PositionRepository;
import org.top.onlinestoreapi.service.ClientService;
import org.top.onlinestoreapi.service.ItemService;
import org.top.onlinestoreapi.service.OrderService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RdbOrderService implements OrderService {

    private final OrderRepository orderRepository;
    private final PositionRepository positionRepository;
    private final ClientService clientService;
    private final ItemService itemService;
    public RdbOrderService(OrderRepository orderRepository, PositionRepository positionRepository, ClientService clientService, ItemService itemService) {
        this.orderRepository = orderRepository;
        this.positionRepository = positionRepository;
        this.clientService = clientService;
        this.itemService = itemService;
    }
    @Override
    public Iterable<Order> getAll() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getById(Integer id) {
        return orderRepository.findById(id);
    }

    @Override
    public Optional<Order> add(Order order) {
        return Optional.of(orderRepository.save(order));
    }

    @Override
    public Optional<Order> update(Order order) {
        Optional<Order> find = orderRepository.findById(order.getId());
        if (find.isPresent()) {
            orderRepository.save(order);
        }
        return find;
    }

    @Override
    public Optional<Order> deleteById(Integer id) {
        Optional<Order> find = orderRepository.findById(id);
        if (find.isPresent()) {
            orderRepository.deleteById(id);
        }
        return find;
    }

    @Override
    public boolean buyItem(Integer itemId, Integer clientId) {
        Optional<Client> client = clientService.getById(clientId);
        if (client.isEmpty()) {
            return false;
        }
        Optional<Item> item = itemService.getById(itemId);
        if (item.isEmpty()) {
            return false;
        }
        Optional<Order> notClosedOrder = findNotClosedOrder(clientId);
        if (notClosedOrder.isPresent()) {
            Optional<Position> position = findPositionByOrderIdAndItemId(notClosedOrder.get().getId(), itemId);
            if (position.isPresent()) {
                position.get().setQuantity(position.get().getQuantity() + 1);
                positionRepository.save(position.get());
            } else {
                Position newPosition = new Position();
                newPosition.setQuantity(1);
                newPosition.setOrder(notClosedOrder.get());
                newPosition.setItem(item.get());
                positionRepository.save(newPosition);
            }
        } else {
            Order newOrder = new Order();
            newOrder.setClient(client.get());
            newOrder = orderRepository.save(newOrder);
            Position position = new Position();
            position.setQuantity(1);
            position.setOrder(newOrder);
            position.setItem(item.get());
            positionRepository.save(position);
        }
        return true;
    }

    @Override
    public Optional<Order> findNotClosedOrder(Integer clientId) {
        Iterable<Order> orders = orderRepository.findAll();
        for (Order order: orders) {
            if (Objects.equals(order.getClient().getId(), clientId) && !order.getClosed()) {
                Set<Position> positionSet = order.getPositionSet();
                order.setPositionSet(positionSet.stream().sorted(Comparator.comparing(op -> op.getItem().getName())).collect(Collectors.toCollection(LinkedHashSet::new)));
                return Optional.of(order);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Position> findPositionByOrderIdAndItemId(Integer orderId, Integer itemId) {
        Iterable<Position> positions = positionRepository.findAll();
        for (Position position : positions) {
            if (position.getOrder().getId().equals(orderId) && position.getItem().getId().equals(itemId)) {
                return Optional.of(position);
            }
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Order> findAllClosedOrders(Integer clientId) {
        Iterable<Order> orders = orderRepository.findAll();
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (Objects.equals(order.getClient().getId(), clientId) && order.getClosed()) {
                result.add(order);
            }
        }
        return result;
    }

    @Override
    public boolean closedOrder(Integer orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty() || order.get().getClosed()) {
            return false;
        }
        order.get().setClosed(true);
        order.get().setDateClosed(new Date());
        orderRepository.save(order.get());
        return true;
    }

    @Override
    public boolean removeItem(Integer positionId) {
        Optional<Position> position = positionRepository.findById(positionId);
        if (position.isPresent()) {
            position.get().setQuantity(position.get().getQuantity() - 1);
            if (position.get().getQuantity() == 0) {
                positionRepository.deleteById(positionId);
            } else {
                positionRepository.save(position.get());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean addItem(Integer positionId) {
        Optional<Position> position = positionRepository.findById(positionId);
        if (position.isPresent()) {
            position.get().setQuantity(position.get().getQuantity() + 1);
            positionRepository.save(position.get());
            return true;
        }
        return false;
    }

}
