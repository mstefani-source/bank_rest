package com.example.bankcards.service;


import com.example.bankcards.dto.CustomerDto;
import com.example.bankcards.entity.Customer;
import com.example.bankcards.entity.mapper.CustomerMapper;
import com.example.bankcards.repository.CustomerRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Log4j2
public class CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerMapper customerMapper;

    public CustomerDto createCustomer(CustomerDto customerDto) {

        Customer customer = customerMapper.mapDtoToModel(customerDto);
        Customer savedCustomer = customerRepository.save(customer);

        return customerMapper.mapModelToDto(savedCustomer);
    }

    public List<CustomerDto> findAllCustomers() {
        List<Customer> customers = customerRepository.findAll();

        return customers
                .stream()
                .map((customer) -> customerMapper.mapModelToDto(customer))
                .toList();
    }

    public Optional<CustomerDto> findById(Long id) {
        return Optional.of(customerMapper.mapModelToDto(customerRepository.findById(id).orElseThrow()));
    }

    public void deleteTarget(Long id) {
        customerRepository.deleteById(id);
    }

    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        Customer customer = customerRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer_id not exist"));
        customer.setName(customerDto.getName());
        return customerMapper.mapModelToDto(customerRepository.save(customer));
    }

    public CustomerDto findByEmail(String email) {
        return customerMapper.mapModelToDto(customerRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("No such customer")));
    }

    public CustomerDto getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByEmail(email);
    }

    public UserDetailsService userDetailsService() {
        return this::findByEmail;
    }
}
