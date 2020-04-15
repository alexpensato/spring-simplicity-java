package org.pensatocode.simplicity.web;

import org.pensatocode.simplicity.jdbc.JdbcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import java.io.Serializable;

@RestController
public abstract class AbstractController<T, ID extends Serializable> {

    private JdbcRepository<T, ID> repository;

    public AbstractController(@Autowired JdbcRepository<T, ID> repository) {
        this.repository = repository;
    }

    @GetMapping
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @GetMapping("/count")
    @ResponseBody
    public Long count() {
        return repository.count();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public T findById(@PathVariable ID id) {
        Assert.notNull(id, "You must provide an ID to locate an item in the repository.");
        return repository.findOne(id);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String delete(@PathVariable ID id) {
        Assert.notNull(id, "You must provide an ID to delete an item from the repository.");
        return "Total itens deleted: " + repository.delete(id);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public String update(@PathVariable ID id, @RequestBody T t) {
        Assert.notNull(id, "You must provide an ID to update an item in the repository.");
        return "Total itens updated: " + repository.update(t, id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public T insert(@RequestBody T t) {
        return repository.save(t);
    }

}
